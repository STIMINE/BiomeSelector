package jp.sohta.biomeselector.item;

import jp.sohta.biomeselector.BiomeSelectorMod;
import jp.sohta.biomeselector.SelectionManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBiomeWand extends Item {
    public ItemBiomeWand() {
        setUnlocalizedName("biomeWand");
        // Reuse the vanilla stick icon so the mod has no required texture asset.
        setTextureName("stick");
        setCreativeTab(CreativeTabs.tabTools);
        setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
            int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            SelectionManager.select(player, x, z);
        }
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        // Sneak + air right-click cancels the player's currently queued large edit.
        if (player.isSneaking()) {
            if (!world.isRemote && player instanceof net.minecraft.entity.player.EntityPlayerMP) {
                BiomeSelectorMod.CHANGE_WORKER.cancel((net.minecraft.entity.player.EntityPlayerMP) player);
            }
            return stack;
        }
        // This method is called for a right-click that did not hit a block (air).
        if (world.isRemote) {
            BiomeSelectorMod.proxy.openBiomeSelector();
        }
        return stack;
    }
}
