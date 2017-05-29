package lime.dumb_miner.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public class ModBlocks {
    public static Block dumb_miner_block;
    public static final void init() {
        dumb_miner_block = new DumbMinerBlock();
        GameRegistry.registerBlock(dumb_miner_block, "dumb_miner_block");

    }
}
