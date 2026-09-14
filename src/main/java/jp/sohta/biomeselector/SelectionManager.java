package jp.sohta.biomeselector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jp.sohta.biomeselector.network.SelectionUpdateMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

/** Server-side, per-player selection state. Y coordinates are deliberately ignored. */
public final class SelectionManager {
    private static final Map<UUID, Selection> SELECTIONS = new HashMap<UUID, Selection>();

    private SelectionManager() { }

    public static void select(EntityPlayer player, int x, int z) {
        UUID id = player.getUniqueID();
        Selection selection = SELECTIONS.get(id);
        int dimension = player.worldObj.provider.dimensionId;
        if (selection == null || selection.dimension != dimension || selection.complete) {
            SELECTIONS.put(id, new Selection(dimension, x, z));
            player.addChatMessage(new ChatComponentText("[Biome Selector] 1点目: " + x + ", " + z));
        } else {
            selection.x2 = x;
            selection.z2 = z;
            selection.complete = true;
            player.addChatMessage(new ChatComponentText("[Biome Selector] 2点目: " + x + ", " + z
                    + " (" + selection.getWidth() + " x " + selection.getDepth() + ")"));
        }
        syncSelection(player);
    }

    public static Selection getComplete(EntityPlayer player) {
        Selection selection = SELECTIONS.get(player.getUniqueID());
        return selection != null && selection.complete
                && selection.dimension == player.worldObj.provider.dimensionId ? selection : null;
    }

    private static void syncSelection(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) return;
        Selection selection = SELECTIONS.get(player.getUniqueID());
        if (selection == null) return;
        BiomeSelectorMod.NETWORK.sendTo(new SelectionUpdateMessage(selection.dimension,
                selection.x1, selection.z1, selection.x2, selection.z2, selection.complete),
                (EntityPlayerMP) player);
    }

    public static void clearClientSelection(EntityPlayerMP player) {
        BiomeSelectorMod.NETWORK.sendTo(SelectionUpdateMessage.clear(player.worldObj.provider.dimensionId), player);
    }

    public static final class Selection {
        public final int dimension;
        public final int x1;
        public final int z1;
        public int x2;
        public int z2;
        private boolean complete;

        private Selection(int dimension, int x, int z) {
            this.dimension = dimension;
            this.x1 = x;
            this.z1 = z;
        }

        public int minX() { return Math.min(x1, x2); }
        public int maxX() { return Math.max(x1, x2); }
        public int minZ() { return Math.min(z1, z2); }
        public int maxZ() { return Math.max(z1, z2); }
        public int getWidth() { return maxX() - minX() + 1; }
        public int getDepth() { return maxZ() - minZ() + 1; }
    }
}
