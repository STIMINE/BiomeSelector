package jp.sohta.biomeselector.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.sohta.biomeselector.client.ClientSelectionState;

/** Server-to-client selection synchronization for the world overlay. */
public class SelectionUpdateMessage implements IMessage {
    private int dimension;
    private int x1;
    private int z1;
    private int x2;
    private int z2;
    private boolean complete;
    private boolean present;

    public SelectionUpdateMessage() { }

    public SelectionUpdateMessage(int dimension, int x1, int z1, int x2, int z2, boolean complete) {
        this.dimension = dimension;
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
        this.complete = complete;
        this.present = true;
    }

    public static SelectionUpdateMessage clear(int dimension) {
        SelectionUpdateMessage message = new SelectionUpdateMessage();
        message.dimension = dimension;
        message.present = false;
        return message;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        dimension = buffer.readInt();
        x1 = buffer.readInt();
        z1 = buffer.readInt();
        x2 = buffer.readInt();
        z2 = buffer.readInt();
        complete = buffer.readBoolean();
        present = buffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(dimension);
        buffer.writeInt(x1);
        buffer.writeInt(z1);
        buffer.writeInt(x2);
        buffer.writeInt(z2);
        buffer.writeBoolean(complete);
        buffer.writeBoolean(present);
    }

    public static class Handler implements IMessageHandler<SelectionUpdateMessage, IMessage> {
        @Override
        public IMessage onMessage(SelectionUpdateMessage message, MessageContext context) {
            ClientSelectionState.set(message.dimension, message.x1, message.z1,
                    message.x2, message.z2, message.complete, message.present);
            return null;
        }
    }
}
