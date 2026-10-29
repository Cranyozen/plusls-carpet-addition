package com.plusls.carpet.impl.network;

import carpet.CarpetServer;
import carpet.patches.EntityPlayerMPFake;
import com.plusls.carpet.PluslsCarpetAdditionExtension;
import com.plusls.carpet.PluslsCarpetAdditionReference;
import com.plusls.carpet.PluslsCarpetAdditionSettings;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.player.PlayerCompat;
import top.hendrixshen.magiclib.api.compat.minecraft.world.level.state.BlockStateCompat;
import top.hendrixshen.magiclib.impl.compat.minecraft.world.level.dimension.DimensionWrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.plusls.carpet.impl.network.packet.pca.*;import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

@SuppressWarnings("removal")public class PcaSyncProtocol {
    public static final ReentrantLock lock = new ReentrantLock(true);
    public static final ReentrantLock pairLock = new ReentrantLock(true);
    // send
    public static final Identifier ENABLE_PCA_SYNC_PROTOCOL = PluslsCarpetAdditionReference.identifier("enable_pca_sync_protocol");
    public static final Identifier DISABLE_PCA_SYNC_PROTOCOL = PluslsCarpetAdditionReference.identifier("disable_pca_sync_protocol");
    public static final Identifier UPDATE_ENTITY = PluslsCarpetAdditionReference.identifier("update_entity");
    public static final Identifier UPDATE_BLOCK_ENTITY = PluslsCarpetAdditionReference.identifier("update_block_entity");
    // recv
    public static final Identifier SYNC_BLOCK_ENTITY = PluslsCarpetAdditionReference.identifier("sync_block_entity");
    public static final Identifier SYNC_ENTITY = PluslsCarpetAdditionReference.identifier("sync_entity");
    public static final Identifier CANCEL_SYNC_BLOCK_ENTITY = PluslsCarpetAdditionReference.identifier("cancel_sync_block_entity");
    public static final Identifier CANCEL_SYNC_ENTITY = PluslsCarpetAdditionReference.identifier("cancel_sync_entity");

    private static final Map<ServerPlayer, Pair<Identifier, BlockPos>> playerWatchBlockPos = new HashMap<>();
    private static final Map<ServerPlayer, Pair<Identifier, Entity>> playerWatchEntity = new HashMap<>();
    private static final Map<Pair<Identifier, BlockPos>, Set<ServerPlayer>> blockPosWatchPlayerSet = new HashMap<>();
    private static final Map<Pair<Identifier, Entity>, Set<ServerPlayer>> entityWatchPlayerSet = new HashMap<>();
    private static final MutablePair<Identifier, Entity> identifierEntityPair = new MutablePair<>();
    private static final MutablePair<Identifier, BlockPos> identifierBlockPosPair = new MutablePair<>();

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(ServerboundCancelSyncBlockEntityPacket.TYPE, ServerboundCancelSyncBlockEntityPacket.CODEC);        PayloadTypeRegistry.serverboundPlay().register(ServerboundCancelSyncEntityPacket.TYPE, ServerboundCancelSyncEntityPacket.CODEC);        PayloadTypeRegistry.serverboundPlay().register(ServerboundSyncBlockEntityPacket.TYPE, ServerboundSyncBlockEntityPacket.CODEC);        PayloadTypeRegistry.serverboundPlay().register(ServerboundSyncEntityPacket.TYPE, ServerboundSyncEntityPacket.CODEC);        PayloadTypeRegistry.clientboundPlay().register(ClientboundDisablePcaSyncProtocolPacket.TYPE, ClientboundDisablePcaSyncProtocolPacket.CODEC);        PayloadTypeRegistry.clientboundPlay().register(ClientboundEnablePcaSyncProtocolPacket.TYPE, ClientboundEnablePcaSyncProtocolPacket.CODEC);        PayloadTypeRegistry.clientboundPlay().register(ClientboundUpdateBlockEntityPacket.TYPE, ClientboundUpdateBlockEntityPacket.CODEC);        PayloadTypeRegistry.clientboundPlay().register(ClientboundUpdateEntityPacket.TYPE, ClientboundUpdateEntityPacket.CODEC);        ServerPlayNetworking.registerGlobalReceiver(ServerboundCancelSyncBlockEntityPacket.TYPE, PcaSyncProtocol::cancelSyncBlockEntityHandler);        ServerPlayNetworking.registerGlobalReceiver(ServerboundCancelSyncEntityPacket.TYPE, PcaSyncProtocol::cancelSyncEntityHandler);        ServerPlayNetworking.registerGlobalReceiver(ServerboundSyncBlockEntityPacket.TYPE, PcaSyncProtocol::syncBlockEntityHandler);        ServerPlayNetworking.registerGlobalReceiver(ServerboundSyncEntityPacket.TYPE, PcaSyncProtocol::syncEntityHandler);        ServerPlayConnectionEvents.JOIN.register(PcaSyncProtocol::onJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(PcaSyncProtocol::onDisconnect);
    }


    private static @NotNull CompoundTag pca$entityToTag(@NotNull Entity entity) {
        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, entity.level().registryAccess());
        entity.saveWithoutId(output);
        return output.buildResult();
    }

    private static boolean pca$isOp(@NotNull MinecraftServer server, @NotNull ServerPlayer player) {
        return server.getProfilePermissions(new NameAndId(player.getGameProfile()))
                .level()
                .ordinal() >= PermissionLevel.GAMEMASTERS.ordinal();
    }

    // 通知客户端服务器已启用 PcaSyncProtocol
    public static void enablePcaSyncProtocol(@NotNull ServerPlayer player) {
        // 在这写如果是在 BC 端的情况下，ServerPlayNetworking.canSend 在这个时机调用会出现错误
        PluslsCarpetAdditionReference.getLogger().debug("Try enablePcaSyncProtocol: {}", player.getName().getString());
        // bc 端比较奇怪，canSend 工作不正常
        // if (ServerPlayNetworking.canSend(player, ENABLE_PCA_SYNC_PROTOCOL)) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ServerPlayNetworking.send(
                player,
                new ClientboundEnablePcaSyncProtocolPacket(buf)        );
        PluslsCarpetAdditionReference.getLogger().debug("send enablePcaSyncProtocol to {}!", player.getName().getString());
        lock.lock();
        lock.unlock();
    }

    // 通知客户端服务器已停用 PcaSyncProtocol
    public static void disablePcaSyncProtocol(@NotNull ServerPlayer player) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ServerPlayNetworking.send(
                player,
                new ClientboundDisablePcaSyncProtocolPacket(buf)        );
        PluslsCarpetAdditionReference.getLogger().debug("send disablePcaSyncProtocol to {}!", player.getName().getString());
    }

    // 通知客户端更新 Entity
    // 包内包含 World 的 Identifier, entityId, entity 的 nbt 数据
    // 传输 World 是为了通知客户端该 Entity 属于哪个 World
    public static void updateEntity(@NotNull ServerPlayer player, @NotNull Entity entity) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeIdentifier(player.level().dimension().identifier());
        buf.writeInt(entity.getId());
        buf.writeNbt(pca$entityToTag(entity));
        ServerPlayNetworking.send(
                player,
                new ClientboundUpdateEntityPacket(buf)        );
    }

    // 通知客户端更新 BlockEntity
    // 包内包含 World 的 Identifier, pos, blockEntity 的 nbt 数据
    // 传输 World 是为了通知客户端该 BlockEntity 属于哪个世界
    public static void updateBlockEntity(@NotNull ServerPlayer player, @NotNull BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();

        // 在生成世界时可能会产生空指针
        if (level == null) {
            return;
        }

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeIdentifier(level.dimension().identifier());
        buf.writeBlockPos(blockEntity.getBlockPos());
        buf.writeNbt(
                blockEntity.saveWithoutMetadata(                        level.registryAccess()                )        );
        ServerPlayNetworking.send(
                player,
                new ClientboundUpdateBlockEntityPacket(buf)        );
    }

    private static void onDisconnect(ServerGamePacketListenerImpl serverPlayNetworkHandler, MinecraftServer minecraftServer) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol) {
            PluslsCarpetAdditionReference.getLogger().debug("onDisconnect remove: {}", serverPlayNetworkHandler.player.getName().getString());
        }
    }

    private static void onJoin(ServerGamePacketListenerImpl serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol) {
            enablePcaSyncProtocol(serverPlayNetworkHandler.player);
        }
    }

    // 客户端通知服务端取消 BlockEntity 同步
    public static void cancelSyncBlockEntityHandler(
            ServerboundCancelSyncBlockEntityPacket packet,            ServerPlayNetworking.Context context    ) {
        if (!PluslsCarpetAdditionSettings.pcaSyncProtocol) {
            return;
        }

        ServerPlayer player = context.player();
        PluslsCarpetAdditionReference.getLogger().debug("{} cancel watch blockEntity.", player.getName().getString());
        PcaSyncProtocol.clearPlayerWatchBlock(player);
    }

    // 客户端通知服务端取消 Entity 同步
    public static void cancelSyncEntityHandler(
            ServerboundCancelSyncEntityPacket packet,            ServerPlayNetworking.Context context    ) {
        if (!PluslsCarpetAdditionSettings.pcaSyncProtocol) {
            return;
        }

        ServerPlayer player = context.player();
        PluslsCarpetAdditionReference.getLogger().debug("{} cancel watch entity.", player.getName().getString());
        PcaSyncProtocol.clearPlayerWatchEntity(player);
    }

    // 客户端请求同步 BlockEntity
    // 包内包含 pos
    // 由于正常的场景一般不会跨世界请求数据，因此包内并不包含 World，以玩家所在的 World 为准
    public static void syncBlockEntityHandler(
            ServerboundSyncBlockEntityPacket packet,            ServerPlayNetworking.Context context    ) {
        if (!PluslsCarpetAdditionSettings.pcaSyncProtocol) {
            return;
        }

        ServerPlayer player = context.player();        BlockPos pos = packet.pos();
        ServerLevel level = (ServerLevel) PlayerCompat.of(player).getLevel();
        BlockState blockState = level.getBlockState(pos);
        BlockStateCompat blockStateCompat = BlockStateCompat.of(blockState);
        clearPlayerWatchData(player);
        PluslsCarpetAdditionReference.getLogger().debug("{} watch blockpos {}: {}", player.getName().getString(), pos, blockState);
        BlockEntity blockEntityAdj = null;

        // 不是单个箱子则需要更新隔壁箱子
        if (blockState.getBlock() instanceof ChestBlock) {
            if (blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                BlockPos posAdj = pos.relative(ChestBlock.getConnectedDirection(blockState));
                // The method in World now checks that the caller is from the same thread...
                blockEntityAdj = level.getChunkAt(posAdj).getBlockEntity(posAdj);
            }
        } else if (PluslsCarpetAdditionReference.tisCarpetLoaded && blockStateCompat.is(Blocks.BARREL) && CarpetServer.settingsManager.getRule("largeBarrel").getBoolValue()) {
            Direction directionOpposite = blockState.getValue(BarrelBlock.FACING).getOpposite();
            BlockPos posAdj = pos.relative(directionOpposite);
            BlockState blockStateAdj = level.getBlockState(posAdj);
            BlockStateCompat blockStateCompatAdj = BlockStateCompat.of(blockStateAdj);

            if (blockStateCompatAdj.is(Blocks.BARREL) && blockStateAdj.getValue(BarrelBlock.FACING) == directionOpposite) {
                blockEntityAdj = level.getChunkAt(posAdj).getBlockEntity(posAdj);
            }
        }

        if (blockEntityAdj != null) {
            updateBlockEntity(player, blockEntityAdj);
        }

        // 本来想判断一下 blockState 类型做个白名单的，考虑到 client 已经做了判断就不在服务端做判断了
        // 就算被恶意攻击应该不会造成什么损失
        // 大不了 op 直接拉黑
        // The method in World now checks that the caller is from the same thread...
        BlockEntity blockEntity = level.getChunkAt(pos).getBlockEntity(pos);
        if (blockEntity != null) {
            updateBlockEntity(player, blockEntity);
        }

        Pair<Identifier, BlockPos> pair = ImmutablePair.of(level.dimension().identifier(), pos);
        lock.lock();
        playerWatchBlockPos.put(player, pair);

        if (!blockPosWatchPlayerSet.containsKey(pair)) {
            blockPosWatchPlayerSet.put(pair, new HashSet<>());
        }

        blockPosWatchPlayerSet.get(pair).add(player);
        lock.unlock();
    }

    // 客户端请求同步 Entity
    // 包内包含 entityId
    // 由于正常的场景一般不会跨世界请求数据，因此包内并不包含 World，以玩家所在的 World 为准
    public static void syncEntityHandler(
            ServerboundSyncEntityPacket packet,            ServerPlayNetworking.Context context    ) {
        if (!PluslsCarpetAdditionSettings.pcaSyncProtocol) {
            return;
        }

        MinecraftServer server = context.server();        ServerPlayer player = context.player();        int entityId = packet.entityId();
        ServerLevel level = (ServerLevel) PlayerCompat.of(player).getLevel();
        Entity entity = level.getEntity(entityId);

        if (entity == null) {
            PluslsCarpetAdditionReference.getLogger().debug("Can't find entity {}.", entityId);
        } else {
            clearPlayerWatchData(player);

            if (entity instanceof Player) {
                if (PluslsCarpetAdditionSettings.pcaSyncPlayerEntity == PluslsCarpetAdditionSettings.PCA_SYNC_PLAYER_ENTITY_OPTIONS.NOBODY) {
                    return;
                } else if (PluslsCarpetAdditionSettings.pcaSyncPlayerEntity == PluslsCarpetAdditionSettings.PCA_SYNC_PLAYER_ENTITY_OPTIONS.BOT) {
                    if (!(entity instanceof EntityPlayerMPFake)) {
                        return;
                    }
                } else if (PluslsCarpetAdditionSettings.pcaSyncPlayerEntity == PluslsCarpetAdditionSettings.PCA_SYNC_PLAYER_ENTITY_OPTIONS.OPS) {
                    if (!(entity instanceof EntityPlayerMPFake) && !pca$isOp(server, player)) {
                        return;
                    }
                } else if (PluslsCarpetAdditionSettings.pcaSyncPlayerEntity == PluslsCarpetAdditionSettings.PCA_SYNC_PLAYER_ENTITY_OPTIONS.OPS_AND_SELF) {
                    if (!(entity instanceof EntityPlayerMPFake) &&
                            !pca$isOp(server, player) &&
                            entity != player) {
                        return;
                    }
                } else if (PluslsCarpetAdditionSettings.pcaSyncPlayerEntity != PluslsCarpetAdditionSettings.PCA_SYNC_PLAYER_ENTITY_OPTIONS.EVERYONE) {
                    // wtf????
                    PluslsCarpetAdditionReference.getLogger().warn("syncEntityHandler wtf???");
                    return;
                }
            }

            PluslsCarpetAdditionReference.getLogger().debug("{} watch entity {}: {}", player.getName().getString(), entityId, entity);
            updateEntity(player, entity);
            Pair<Identifier, Entity> pair = ImmutablePair.of(level.dimension().identifier(), entity);
            lock.lock();
            playerWatchEntity.put(player, pair);

            if (!entityWatchPlayerSet.containsKey(pair)) {
                entityWatchPlayerSet.put(pair, new HashSet<>());
            }

            entityWatchPlayerSet.get(pair).add(player);
            lock.unlock();
        }
    }

    private static MutablePair<Identifier, Entity> getIdentifierEntityPair(Identifier identifier, Entity entity) {
        pairLock.lock();
        identifierEntityPair.setLeft(identifier);
        identifierEntityPair.setRight(entity);
        pairLock.unlock();
        return identifierEntityPair;
    }

    private static MutablePair<Identifier, BlockPos> getIdentifierBlockPosPair(Identifier identifier, BlockPos pos) {
        pairLock.lock();
        identifierBlockPosPair.setLeft(identifier);
        identifierBlockPosPair.setRight(pos);
        pairLock.unlock();
        return identifierBlockPosPair;
    }

    // 工具
    private static @Nullable Set<ServerPlayer> getWatchPlayerList(@NotNull Entity entity) {
        return entityWatchPlayerSet.get(getIdentifierEntityPair(entity.level().dimension().identifier(), entity));
    }

    private static @Nullable Set<ServerPlayer> getWatchPlayerList(@NotNull Level world, @NotNull BlockPos blockPos) {
        return entityWatchPlayerSet.get(getIdentifierBlockPosPair(world.dimension().identifier(), blockPos));
    }

    public static boolean syncEntityToClient(@NotNull Entity entity) {
        if (entity.level().isClientSide()) {
            return false;
        }

        lock.lock();
        Set<ServerPlayer> playerList = getWatchPlayerList(entity);
        boolean ret = false;

        if (playerList != null) {
            for (ServerPlayer player : playerList) {
                updateEntity(player, entity);
                ret = true;
            }
        }

        lock.unlock();
        return ret;
    }

    public static boolean syncBlockEntityToClient(@NotNull BlockEntity blockEntity) {
        boolean ret = false;
        Level world = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();

        // 在生成世界时可能会产生空指针
        if (world != null) {
            if (world.isClientSide()) {
                return false;
            }

            BlockState blockState = world.getBlockState(pos);
            BlockStateCompat blockStateCompat = BlockStateCompat.of(blockState);
            lock.lock();
            Set<ServerPlayer> playerList = getWatchPlayerList(world, blockEntity.getBlockPos());
            Set<ServerPlayer> playerListAdj = null;

            if (blockState.getBlock() instanceof ChestBlock) {
                if (blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                    // 如果是一个大箱子需要特殊处理
                    // 上面不用 isOf 是为了考虑到陷阱箱的情况，陷阱箱继承自箱子
                    BlockPos posAdj = pos.relative(ChestBlock.getConnectedDirection(blockState));
                    playerListAdj = getWatchPlayerList(world, posAdj);
                }
            } else if (PluslsCarpetAdditionReference.tisCarpetLoaded && blockStateCompat.is(Blocks.BARREL) && CarpetServer.settingsManager.getRule("largeBarrel").getBoolValue()) {
                Direction directionOpposite = blockState.getValue(BarrelBlock.FACING).getOpposite();
                BlockPos posAdj = pos.relative(directionOpposite);
                BlockState blockStateAdj = world.getBlockState(posAdj);
                BlockStateCompat blockStateCompatAdj = BlockStateCompat.of(blockStateAdj);

                if (blockStateCompatAdj.is(Blocks.BARREL) && blockStateAdj.getValue(BarrelBlock.FACING) == directionOpposite) {
                    playerListAdj = getWatchPlayerList(world, posAdj);
                }
            }

            if (playerListAdj != null) {
                if (playerList == null) {
                    playerList = playerListAdj;
                } else {
                    playerList.addAll(playerListAdj);
                }
            }

            if (playerList != null) {
                for (ServerPlayer player : playerList) {
                    updateBlockEntity(player, blockEntity);
                    ret = true;
                }
            }

            lock.unlock();
        }
        return ret;
    }

    private static void clearPlayerWatchEntity(ServerPlayer player) {
        lock.lock();
        Pair<Identifier, Entity> pair = playerWatchEntity.get(player);

        if (pair != null) {
            Set<ServerPlayer> playerSet = entityWatchPlayerSet.get(pair);
            playerSet.remove(player);

            if (playerSet.isEmpty()) {
                entityWatchPlayerSet.remove(pair);
            }

            playerWatchEntity.remove(player);
        }

        lock.unlock();
    }

    private static void clearPlayerWatchBlock(ServerPlayer player) {
        lock.lock();
        Pair<Identifier, BlockPos> pair = playerWatchBlockPos.get(player);

        if (pair != null) {
            Set<ServerPlayer> playerSet = blockPosWatchPlayerSet.get(pair);
            playerSet.remove(player);

            if (playerSet.isEmpty()) {
                blockPosWatchPlayerSet.remove(pair);
            }

            playerWatchBlockPos.remove(player);
        }

        lock.unlock();
    }

    // 停用 PcaSyncProtocol
    public static void disablePcaSyncProtocolGlobal() {
        lock.lock();
        playerWatchBlockPos.clear();
        playerWatchEntity.clear();
        blockPosWatchPlayerSet.clear();
        entityWatchPlayerSet.clear();
        lock.unlock();

        if (PluslsCarpetAdditionExtension.getServer() != null) {
            for (ServerPlayer player : PluslsCarpetAdditionExtension.getServer().getPlayerList().getPlayers()) {
                disablePcaSyncProtocol(player);
            }
        }
    }

    // 启用 PcaSyncProtocol
    public static void enablePcaSyncProtocolGlobal() {
        if (PluslsCarpetAdditionExtension.getServer() == null) {
            return;
        }

        for (ServerPlayer player : PluslsCarpetAdditionExtension.getServer().getPlayerList().getPlayers()) {
            enablePcaSyncProtocol(player);
        }
    }

    // 删除玩家数据
    public static void clearPlayerWatchData(ServerPlayer player) {
        PcaSyncProtocol.clearPlayerWatchBlock(player);
        PcaSyncProtocol.clearPlayerWatchEntity(player);
    }
}
