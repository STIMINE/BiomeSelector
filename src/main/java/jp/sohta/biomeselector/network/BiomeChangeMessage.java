package jp.sohta.biomeselector.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.sohta.biomeselector.BiomeSelectorMod;

public class BiomeChangeMessage implements IMessage {
    private int biomeId;

    public BiomeChangeMessage() { }

    public BiomeChangeMessage(int biomeId) {
        this.biomeId = biomeId;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        biomeId = buffer.readInt();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(biomeId);
    }

    public static class Handler implements IMessageHandler<BiomeChangeMessage, IMessage> {
        @Override
        public IMessage onMessage(final BiomeChangeMessage message, final MessageContext context) {
            // Forge 1.7.10 has no IThreadListener scheduler. The SimpleImpl
            // server handler runs this operation against the server player.
            BiomeSelectorMod.CHANGE_WORKER.enqueue(context.getServerHandler().playerEntity, message.biomeId);
            return null;
        }
    }
}
