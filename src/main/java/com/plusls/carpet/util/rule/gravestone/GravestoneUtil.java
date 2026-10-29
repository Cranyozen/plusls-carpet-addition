package com.plusls.carpet.util.rule.gravestone;

import com.plusls.carpet.PluslsCarpetAdditionReference;
import com.plusls.carpet.mixin.accessor.AccessorSkullBlockEntity;
import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GravestoneUtil {
    public static final int NETHER_BEDROCK_MAX_Y = 127;
    public static final int SEARCH_RANGE = 5;
    public static final int PLAYER_INVENTORY_SIZE = 41;

    public static void init() {
        ServerPlayerEvents.ALLOW_DEATH.register((player, damageSource, damageAmount) -> {
            GravestoneUtil.deathHandle(player);
            return true;
        });
    }

    public static void deathHandle(@NotNull ServerPlayer player) {
        if (PluslsCarpetAdditionSettings.pcaDebug) {
            PluslsCarpetAdditionReference.getLogger().info("gravestone: death handle for {}", player.getName().getString());
        }

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        if (!PluslsCarpetAdditionSettings.gravestone ||
                level.getGameRules().get(GameRules.KEEP_INVENTORY)) {
            return;
        }

        for (InteractionHand hand : InteractionHand.values()) {
            if (player.getItemInHand(hand).is(Items.TOTEM_OF_UNDYING)) {
                return;
            }
        }

        player.destroyVanishingCursedItems();

        SimpleContainer inventory = new SimpleContainer(PLAYER_INVENTORY_SIZE);
        Inventory playerInventory = player.getInventory();

        for (int slot = 0; slot < playerInventory.getContainerSize(); ++slot) {
            ItemStack itemStack = playerInventory.getItem(slot);

            if (!itemStack.isEmpty()) {
                inventory.addItem(itemStack.copy());
            }
        }

        int xp = Math.max(0, player.totalExperience / 2);
        playerInventory.clearContent();
        // 经验整体搬进墓碑，人物身上三个字段一起清空，避免“保留 totalExperience”
        // 导致复活后经验被重复计算（原版只按 experienceLevel 掉经验，不清会出现半份重复）。
        player.experienceLevel = 0;
        player.experienceProgress = 0.0F;
        player.totalExperience = 0;

        BlockPos gravePos = findGravePos(player);
        MinecraftServer server = level.getServer();

        if (server != null) {
            DeathInfo deathInfo = new DeathInfo(System.currentTimeMillis(), xp, inventory,
                    Math.max(0, PluslsCarpetAdditionSettings.gravestoneProtectionCount));

            if (PluslsCarpetAdditionSettings.pcaDebug) {
                PluslsCarpetAdditionReference.getLogger().info(
                        "gravestone: placing grave at {}, xp={}, items={}, protection={}",
                        gravePos, xp, inventory.getContainerSize(), deathInfo.getProtection());
            }

            server.submit(placeGraveRunnable(level, gravePos, deathInfo, player));
        }
    }

    // find pos to place gravestone
    public static BlockPos findGravePos(@NotNull ServerPlayer player) {
        BlockPos.MutableBlockPos playerPos = player.blockPosition().mutable();
        playerPos.setY(GravestoneUtil.clampY(player, playerPos.getY()));

        if (GravestoneUtil.canPlaceGrave(player, playerPos)) {
            return playerPos;
        }

        BlockPos.MutableBlockPos gravePos = new BlockPos.MutableBlockPos();

        for (int x = playerPos.getX() + SEARCH_RANGE; x >= playerPos.getX() - SEARCH_RANGE; x--) {
            gravePos.setX(x);
            int minY = clampY(player, playerPos.getY() - SEARCH_RANGE);

            for (int y = clampY(player, playerPos.getY() + SEARCH_RANGE); y >= minY; y--) {
                gravePos.setY(y);

                for (int z = playerPos.getZ() + SEARCH_RANGE; z >= playerPos.getZ() - SEARCH_RANGE; z--) {
                    gravePos.setZ(z);

                    if (canPlaceGrave(player, gravePos)) {
                        return drop(player, gravePos);
                    }
                }
            }
        }

        // search up
        gravePos.set(playerPos);

        while (player.level().getBlockState(gravePos).getBlock() == Blocks.BEDROCK) {
            gravePos.setY(gravePos.getY() + 1);
        }

        return gravePos;
    }

    // make sure to spawn graves on the suitable place
    public static int clampY(@NotNull ServerPlayer player, int y) {
        Level level = player.level();

        //don't spawn on nether ceiling, unless the player is already there.
        if (level.dimension() == Level.NETHER && y < NETHER_BEDROCK_MAX_Y) {
            //clamp to 1 -- don't spawn graves the layer right above the void, so players can actually recover their items.
            return Mth.clamp(y, level.getMinY() + 1, NETHER_BEDROCK_MAX_Y - 1);
        } else {
            return Mth.clamp(y, level.getMinY() + 1, level.getMaxY() - 1);
        }
    }

    public static boolean canPlaceGrave(@NotNull ServerPlayer player, BlockPos pos) {
        Level level = player.level();
        BlockState state = level.getBlockState(pos);

        if (pos.getY() <= level.getMinY() + 1 || pos.getY() >= level.getMaxY() - 1) {
            return false;
        } else if (state.isAir()) {
            return true;
        } else { // block can replace
            return state.canBeReplaced(new DirectionalPlaceContext(level, pos,
                    Direction.DOWN, ItemStack.EMPTY, Direction.UP));
        }
    }

    // players are blown up
    // reduce y pos
    public static BlockPos drop(@NotNull ServerPlayer player, BlockPos pos) {
        Level level = player.level();
        BlockPos.MutableBlockPos searchPos = new BlockPos.MutableBlockPos().set(pos);
        int i = 0;

        for (int y = pos.getY() - 1; y > level.getMinY() + 1 && i < 10; y--) {
            i++;
            searchPos.setY(clampY(player, y));

            if (!level.getBlockState(searchPos).isAir()) {
                searchPos.setY(clampY(player, y + 1));
                return searchPos;
            }
        }

        return pos;
    }

    /**
     * 把墓碑里的物品和经验一次性吐出来。
     * 有主（玩家挖的）就把经验直接加进玩家身上，避免掉一堆经验球造成卡顿；
     * 无主（爆炸 / 活塞 / 其它模组）时只掉**一个**经验球，同样不刷球。
     */
    public static void lootGrave(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull DeathInfo deathInfo) {
        lootGrave(level, pos, deathInfo, null);
    }

    public static void lootGrave(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull DeathInfo deathInfo,
                                 @Nullable ServerPlayer receiver) {
        for (ItemStack itemStack : deathInfo.inventory.removeAllItems()) {
            Block.popResource(level, pos, itemStack);
        }

        int xp = deathInfo.xp;

        if (xp > 0) {
            if (receiver != null && !receiver.isSpectator() && !receiver.isRemoved()) {
                receiver.giveExperiencePoints(xp);
            } else {
                // 兜底：只生成一个经验球，不再按 getExperienceValue 拆成一把
                level.addFreshEntity(new ExperienceOrb(level, pos.getX() + 0.5D, pos.getY() + 0.5D,
                        pos.getZ() + 0.5D, xp));
            }
        }

        if (PluslsCarpetAdditionSettings.pcaDebug) {
            PluslsCarpetAdditionReference.getLogger().info("gravestone: loot at {}, xp={}, receiver={}",
                    pos, xp, receiver == null ? "none" : receiver.getGameProfile().name());
        }
    }

    /**
     * 墓碑被「非玩家」手段摧毁（爆炸、活塞……）：先把内容掉出来，再由我们自己移除方块。
     * 直接 removeBlock 不会走原版掉落表，所以**不会掉一个玩家头物品**，
     * 这也是和原版「炸掉一个头掉一个头」的区别所在。
     */
    public static void destroyGrave(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                    @NotNull GravesStoneSkullBlockEntity gravestone) {
        DeathInfo deathInfo = gravestone.pca$getDeathInfo();

        if (deathInfo == null) {
            return;
        }

        // 先清数据，保证随后 removeBlock 触发的 preRemoveSideEffects 不会重复结算
        gravestone.pca$setDeathInfo(null);
        gravestone.pca$markLooted();
        level.removeBlock(pos, false);

        // 推迟一 tick 再掉落：爆炸的伤害阶段就在当前拍，物品此刻生成会被自己这发爆炸炸掉
        BlockPos lootPos = pos.immutable();
        PendingGraveLoot.enqueue(() -> lootGrave(level, lootPos, deathInfo, null));

        if (PluslsCarpetAdditionSettings.pcaDebug) {
            PluslsCarpetAdditionReference.getLogger().info("gravestone: destroyed at {}, no skull drop", pos);
        }
    }

    /**
     * 墓碑的「保护」判定：爆炸、活塞等非玩家破坏在真正动手前先问这里。
     * 返回 true 表示这一次破坏被挡下（扣掉一次保护次数），方块应当保持原样。
     * 返回 false 表示保护已经耗尽，交给原版处理；内容由
     * {@code MixinGraveBlockEntityRemoval} 在方块被替换时统一结算，不会丢。
     */
    public static boolean protectGrave(@NotNull Level level, @NotNull BlockPos pos,
                                       @NotNull GravesStoneSkullBlockEntity gravestone) {
        DeathInfo deathInfo = gravestone.pca$getDeathInfo();

        if (deathInfo == null) {
            return false;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }

        if (!deathInfo.tryBlockDamage(serverLevel.getGameTime())) {
            if (PluslsCarpetAdditionSettings.pcaDebug) {
                PluslsCarpetAdditionReference.getLogger().info("gravestone: protection exhausted at {}", pos);
            }

            return false;
        }

        if (gravestone instanceof BlockEntity blockEntity) {
            blockEntity.setChanged();
        }

        if (PluslsCarpetAdditionSettings.pcaDebug) {
            PluslsCarpetAdditionReference.getLogger().info("gravestone: protected at {}, protection left={}",
                    pos, deathInfo.getProtection());
        }

        return true;
    }

    /**
     * 方块实体每 tick 调用：只有墓碑被水 / 岩浆泡着（或贴着）时才推进倒计时，
     * 到 {@code gravestoneFluidGraceSeconds} 秒就把内容结算掉并让方块消失（不掉头颅）。
     * 流体离开后默认冻结倒计时；{@code gravestoneFluidGraceReset} 打开则清零重来。
     */
    public static void tickGrave(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                 @NotNull GravesStoneSkullBlockEntity gravestone) {
        DeathInfo deathInfo = gravestone.pca$getDeathInfo();

        if (deathInfo == null) {
            return;
        }

        int graceSeconds = PluslsCarpetAdditionSettings.gravestoneFluidGraceSeconds;

        if (graceSeconds <= 0) {
            return;
        }

        if (!isFluidAttacking(level, pos)) {
            if (PluslsCarpetAdditionSettings.gravestoneFluidGraceReset && deathInfo.getFluidGraceAge() != 0) {
                deathInfo.resetFluidGraceAge();
                markChanged(gravestone);
            }

            return;
        }

        deathInfo.addFluidGraceAge();

        if (deathInfo.getFluidGraceAge() >= graceSeconds * 20) {
            if (PluslsCarpetAdditionSettings.pcaDebug) {
                PluslsCarpetAdditionReference.getLogger().info(
                        "gravestone: fluid grace expired at {} after {} ticks", pos, deathInfo.getFluidGraceAge());
            }

            destroyGrave(level, pos, gravestone);
            return;
        }

        if (deathInfo.getFluidGraceAge() % 20 == 0) {
            markChanged(gravestone);
        }
    }

    /**
     * 判断附近的流体到底会不会「冲坏」墓碑，等价于原版的流动判定：
     * <ul>
     *     <li>正上方的流体一定会往下灌 → 算威胁；</li>
     *     <li>正下方的流体不会往上流 → 不算；</li>
     *     <li>侧面的<b>源方块</b>会朝四个方向扩散 → 算；</li>
     *     <li>侧面的<b>流动水/岩浆</b>先看水位：{@code getAmount() == 1}（等价 {@code LEVEL == 7}，
     *     最外层薄水）不算；其余再看向量，只有真的朝墓碑流才算。</li>
     * </ul>
     * 平台边缘顺着边往下淌的水（流向背离墓碑）因此不会被误判。
     */
    public static boolean isFluidAttacking(@NotNull ServerLevel level, @NotNull BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos near = pos.relative(direction);
            FluidState fluidState = level.getFluidState(near);

            if (fluidState.isEmpty()) {
                continue;
            }

            if (direction == Direction.UP) {
                return true;
            }

            if (direction == Direction.DOWN) {
                continue;
            }

            if (fluidState.isSource()) {
                return true;
            }

            // 侧面流动流体还要看水位：getAmount() == 1 等价于 LiquidBlock.LEVEL == 7，
            // 即最外层那一圈薄水（贴地流动的边缘水），它推不动方块，不算威胁。
            if (fluidState.getAmount() <= 1) {
                continue;
            }

            Vec3 flow = fluidState.getFlow(level, near);
            Vec3 toward = Vec3.atLowerCornerOf(direction.getOpposite().getUnitVec3i());

            if (flow.dot(toward) > 0.1D) {
                return true;
            }
        }

        return false;
    }

    private static void markChanged(@NotNull GravesStoneSkullBlockEntity gravestone) {
        if (gravestone instanceof BlockEntity blockEntity) {
            blockEntity.setChanged();
        }
    }

    @Contract(pure = true)
    public static @NotNull Runnable placeGraveRunnable(Level world, BlockPos pos, DeathInfo deathInfo, ServerPlayer player) {
        return () -> {
            BlockState graveBlock = Blocks.PLAYER_HEAD.defaultBlockState();

            // avoid setblockstate fail.
            while (!world.setBlockAndUpdate(pos, graveBlock)) {
                PluslsCarpetAdditionReference.getLogger().warn(String.format("set gravestone at %d %d %d fail, try again.",
                        pos.getX(), pos.getY(), pos.getZ()));
            }

            if (world.getBlockEntity(pos) instanceof SkullBlockEntity graveEntity) {
                ((GravesStoneSkullBlockEntity) graveEntity).pca$setDeathInfo(deathInfo);
                ((AccessorSkullBlockEntity) graveEntity).pca$setOwnerProfile(
                        ResolvableProfile.createResolved(player.getGameProfile()));
                graveEntity.setChanged();
            }

            notifyGravePosition(world, pos, player);
        };
    }

    /**
     * 规则 {@code gravestoneNotifyPosition}：墓碑放下后，把坐标发到死亡玩家的聊天栏。
     * 死亡界面期间玩家对象仍在线；如果已经重生 / 重连，则按 UUID 取当前在线的那一个。
     */
    private static void notifyGravePosition(@NotNull Level world, @NotNull BlockPos pos, @NotNull ServerPlayer player) {
        if (!PluslsCarpetAdditionSettings.gravestoneNotifyPosition) {
            return;
        }

        try {
            MinecraftServer server = world.getServer();
            ServerPlayer target = server == null ? null : server.getPlayerList().getPlayer(player.getUUID());

            if (target == null || target.isRemoved()) {
                return;
            }

            Component message = Component.translatable("pca.message.gravestone.position",
                    Component.translatable(world.dimension().identifier().toLanguageKey("dimension")),
                    pos.getX(), pos.getY(), pos.getZ());

            target.sendSystemMessage(message);

            if (PluslsCarpetAdditionSettings.pcaDebug) {
                PluslsCarpetAdditionReference.getLogger().info("gravestone: notified {} at {} {} {}",
                        target.getGameProfile().name(), pos.getX(), pos.getY(), pos.getZ());
            }
        } catch (Exception exception) {
            // 假人 / 断线等场景下发送失败不应该影响墓碑生成
            PluslsCarpetAdditionReference.getLogger().debug("gravestone: failed to notify grave position", exception);
        }
    }
}
