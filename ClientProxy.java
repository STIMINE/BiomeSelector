package jp.sohta.biomeselector;

import net.minecraft.client.Minecraft;
import jp.sohta.biomeselector.client.GuiBiomeSelector;
import jp.sohta.biomeselector.client.SelectionRenderer;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    @Override
    public void registerClientHandlers() {
        MinecraftForge.EVENT_BUS.register(new SelectionRenderer());
    }

    @Override
    public void openBiomeSelector() {
        Minecraft.getMinecraft().displayGuiScreen(new GuiBiomeSelector());
    }
}
