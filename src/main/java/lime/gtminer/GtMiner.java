package lime.gtminer;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import lime.gtminer.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

import java.util.Hashtable;

@Mod(modid = GtMiner.MODID, name = GtMiner.MODNAME, version = GtMiner.VERSION)
public class GtMiner {

    public static final String MODID = "gtminer";
    public static final String MODNAME = "GT Miner";
    public static final String VERSION = "1.7.10-1";
    public static final String DEPENDENCIES = "required-after:IC2; required-after:gregtech";

    @Mod.Instance
    public static GtMiner instance = new GtMiner();
    public static Hashtable<String, String> chunk_cache = new Hashtable<String, String>();
    public static String current_chunk_cache_data = "";
    public static void updateCurrentChunkCacheData(String key){
        current_chunk_cache_data = GtMiner.chunk_cache.get(key);
        if (current_chunk_cache_data == null) current_chunk_cache_data = "";
    }
    public static String[] get_current_chunk_cache_data(){
        return (GtMiner.current_chunk_cache_data).split("\\n");
    }

    public static final CreativeTabs ctab = new CreativeTabs("GtMiner"){
        @Override public Item getTabIconItem() {
            return Items.diamond;
        }
    };

    @SidedProxy(clientSide="lime.gtminer.proxy.ClientProxy", serverSide="lime.gtminer.proxy.ServerProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
    }
}