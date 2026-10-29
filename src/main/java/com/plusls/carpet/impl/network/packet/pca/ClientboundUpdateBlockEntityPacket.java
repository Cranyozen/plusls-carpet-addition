package com.plusls.carpet.impl.network.packet.pca;

import com.plusls.carpet.impl.network.PcaSyncProtocol;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record ClientboundUpdateBlockEntityPacket(Identifier dimension, BlockPos blockPos,
                                                 CompoundTag tag) implements CustomPacketPayload {
    public static final Type<ClientboundUpdateBlockEntityPacket> TYPE = new Type<>(PcaSyncProtocol.UPDATE_BLOCK_ENTITY);
    public static final StreamCodec<FriendlyByteBuf, ClientboundUpdateBlockEntityPacket> CODEC = CustomPacketPayload.codec(
            ClientboundUpdateBlockEntityPacket::write, ClientboundUpdateBlockEntityPacket::new);

    public ClientboundUpdateBlockEntityPacket(@NotNull FriendlyByteBuf byteBuf) {
        this(byteBuf.readIdentifier(), byteBuf.readBlockPos(), byteBuf.readNbt());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ClientboundUpdateBlockEntityPacket.TYPE;
    }

    private void write(@NotNull FriendlyByteBuf byteBuf) {
        byteBuf.writeIdentifier(this.dimension);
        byteBuf.writeBlockPos(this.blockPos);
        byteBuf.writeNbt(this.tag);
    }
}
