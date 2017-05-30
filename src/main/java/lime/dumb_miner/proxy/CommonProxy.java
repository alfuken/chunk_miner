package lime.dumb_miner.proxy;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.enums.ItemList;
import ic2.core.Ic2Items;
import lime.dumb_miner.ChunkLoadingCallback;
import lime.dumb_miner.Config;
import lime.dumb_miner.DumbMiner;
import lime.dumb_miner.blocks.ModBlocks;
import lime.dumb_miner.items.ModItems;
import lime.dumb_miner.tiles.ModTiles;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

import static lime.dumb_miner.DumbMiner.config;
import static lime.dumb_miner.DumbMiner.logger;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
        File directory = e.getModConfigurationDirectory();
        config = new Configuration(new File(directory.getPath(), "dumb_miner.cfg"));
        Config.readConfig();

        if (Config.load_chunks){
            ForgeChunkManager.setForcedChunkLoadingCallback(DumbMiner.INSTANCE, new ChunkLoadingCallback());
        }

        ModItems.init();
        ModBlocks.init();
        ModTiles.init();
    }

    public void init(FMLInitializationEvent e) {
        GameRegistry.addRecipe(new ItemStack(ModItems.scanner),
                "DS",
                "SD",
                'S', Items.stick, 'D', Blocks.dirt
        );

//        GameRegistry.addRecipe(new ItemStack(ModItems.area_scanner),
//                "RRR",
//                "RDR",
//                "RRR",
//                'D', Blocks.dirt, 'R', ModItems.scanner
//        );

        Item hull;
        if (Loader.isModLoaded("gregtech")){
            hull = ItemList.Hull_Bronze.getItem();
        } else {
            hull = Ic2Items.machine.getItem();
        }

        GameRegistry.addRecipe(new ItemStack(ModBlocks.dumb_miner_block),
            "DHS",
            "PPP",
            "SPD",
            'S', Items.stick, 'D', Blocks.dirt, 'H', hull, 'P', Ic2Items.platebronze
        );

    }

    public void postInit(FMLPostInitializationEvent e) {
//        MinecraftForge.EVENT_BUS.register(new RenderGuiHandler());
        if (config.hasChanged()) {
            config.save();
        }
    }
}