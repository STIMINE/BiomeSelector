package jp.sohta.biomeselector;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.common.registry.GameRegistry;
import jp.sohta.biomeselector.item.ItemBiomeWand;
import jp.sohta.biomeselector.network.BiomeChangeMessage;
import jp.sohta.biomeselector.network.SelectionUpdateMessage;
import net.minecraftforge.common.config.Configuration;

@Mod(modid = BiomeSelectorMod.MOD_ID, name = "Biome Selector", version = "1.3.0")
public class BiomeSelectorMod {
    public static final String MOD_ID = "biomeselector";

    @Mod.Instance(MOD_ID)
    public static BiomeSelectorMod instance;

    @SidedProxy(clientSide = "jp.sohta.biomeselector.ClientProxy", serverSide = "jp.sohta.biomeselector.CommonProxy")
    public static CommonProxy proxy;

    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
    public static ItemBiomeWand biomeWand;
    public static final BiomeChangeWorker CHANGE_WORKER = new BiomeChangeWorker();
    public static int maxAreaBlocks = 1000000;
    public static int chunksPerTick = 8;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        maxAreaBlocks = config.getInt("MaxAreaBlocks", Configuration.CATEGORY_GENERAL, 1000000, 1,
                Integer.MAX_VALUE, "Maximum selected X/Z area in blocks (1000 x 1000 = 1000000).");
        chunksPerTick = config.getInt("ChunksPerTick", Configuration.CATEGORY_GENERAL, 8, 1, 1024,
                "Maximum chunks changed per server tick. Higher values finish faster but may cause lag.");
        if (config.hasChanged()) config.save();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        biomeWand = new ItemBiomeWand();
        GameRegistry.registerItem(biomeWand, "biomeWand");
        NETWORK.registerMessage(BiomeChangeMessage.Handler.class, BiomeChangeMessage.class, 0, Side.SERVER);
        NETWORK.registerMessage(SelectionUpdateMessage.Handler.class, SelectionUpdateMessage.class, 1, Side.CLIENT);
        FMLCommonHandler.instance().bus().register(CHANGE_WORKER);
        proxy.registerClientHandlers();
    }
}
