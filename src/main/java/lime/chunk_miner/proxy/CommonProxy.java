package lime.chunk_miner.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.registry.GameRegistry;
import ic2.core.Ic2Items;
import lime.chunk_miner.events.ChunkLoadingCallback;
import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.Config;
import lime.chunk_miner.blocks.ModBlocks;
import lime.chunk_miner.items.ModItems;
import lime.chunk_miner.tiles.ModTiles;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.io.File;

import static lime.chunk_miner.ChunkMiner.config;
import static lime.chunk_miner.ChunkMiner.logger;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
        File directory = e.getModConfigurationDirectory();
        config = new Configuration(new File(directory.getPath(), "chunk_miner.cfg"));
        Config.readConfig();

        if (Config.load_chunks){
            ForgeChunkManager.setForcedChunkLoadingCallback(ChunkMiner.INSTANCE, new ChunkLoadingCallback());
        }

        ModItems.init();
        ModBlocks.init();
        ModTiles.init();
    }

    public void init(FMLInitializationEvent e) {
        GameRegistry.addRecipe(new ItemStack(ModItems.chunk_scanner),
                "DS",
                "SD",
                'S', Items.stick, 'D', Blocks.dirt
        );

        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.area_scanner),
                ModItems.chunk_scanner,
                Items.apple
        );
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.area_scanner),
                ModItems.chunk_scanner,
                ModItems.chunk_scanner,
                ModItems.chunk_scanner,
                Items.egg
        );

        GameRegistry.addRecipe(new ItemStack(ModItems.scan_registry),
                "MCB",
                "PAS",
                "DKI",
                'M', Items.map,
                'C', Items.compass,
                'B', Items.writable_book,
                'P', Items.paper,
                'A', Items.apple,
                'S', ModItems.chunk_scanner,
                'D', Blocks.dirt,
                'K', Items.redstone,
                'I', Items.stick

        );

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.dumb_miner_block),
                "SLB",
                "RMR",
                "DKI",
                'S', ModItems.chunk_scanner,
                'L', Blocks.lever,
                'B', Blocks.wooden_button,
                'R', "plateBronze",
                'M', Ic2Items.machine.getItem(),
                'D', Blocks.dirt,
                'K', Items.redstone,
                'I', Items.stick
        ));

    }

    public void postInit(FMLPostInitializationEvent e) {
//        MinecraftForge.EVENT_BUS.register(new RenderGuiHandler());
        if (config.hasChanged()) {
            config.save();
        }
    }

    public EntityPlayer getPlayer(MessageContext ctx){
        return ctx.getServerHandler().playerEntity;
    }
}