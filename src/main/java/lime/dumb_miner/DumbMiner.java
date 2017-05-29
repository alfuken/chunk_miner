package lime.dumb_miner;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import lime.dumb_miner.items.ModItems;
import lime.dumb_miner.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

@Mod(modid = DumbMiner.MODID, name = DumbMiner.MODNAME, version = DumbMiner.VERSION)
public class DumbMiner {

    public static final String MODID = "dumb_miner";
    public static final String MODNAME = "Dumb Miner";
    public static final String VERSION = "1.7.10-1";
//    public static final String DEPENDENCIES = "required-after:IC2; required-after:gregtech";
    public static final String DEPENDENCIES = "required-after:IC2";
    public static Logger logger;
    public static Configuration config;

    @Mod.Instance
    public static DumbMiner INSTANCE = new DumbMiner();

    public static CreativeTabs ctab = new CreativeTabs("DumbMiner"){
        @Override public Item getTabIconItem() {
            return ModItems.scanner;
        }
    };

    @SidedProxy(clientSide="lime.dumb_miner.proxy.ClientProxy", serverSide="lime.dumb_miner.proxy.ServerProxy")
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
