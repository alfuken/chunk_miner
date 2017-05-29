package lime.dumb_miner.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import ic2.core.Ic2Items;
import lime.dumb_miner.blocks.DumbMinerBlock;
import lime.dumb_miner.items.DumbScanner;
import lime.dumb_miner.tiles.DumbMinerTile;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent e) {
        Item scanner = new DumbScanner();
        Block minerblock = new DumbMinerBlock();
        GameRegistry.registerItem(scanner, "dumb_scanner");
        GameRegistry.registerBlock(minerblock, "dumb_miner_block");
        GameRegistry.registerTileEntity(DumbMinerTile.class, "dumb_miner_tile");
        GameRegistry.addRecipe(new ItemStack(scanner),
            "DS",
            "SD",
            'S', Items.stick, 'D', Blocks.dirt
        );
        GameRegistry.addRecipe(new ItemStack(Item.getItemFromBlock(minerblock)),
            "DSD",
            "SDS",
            "RBR",
            'S', Items.stick, 'D', Blocks.dirt, 'R', scanner, 'B', Ic2Items.bronzeBlock
        );

    }

    public void init(FMLInitializationEvent e) {
//        MinecraftForge.EVENT_BUS.register(new EnteringChunkEventHandler());
    }

    public void postInit(FMLPostInitializationEvent e) {
//        MinecraftForge.EVENT_BUS.register(new RenderGuiHandler());
    }
}