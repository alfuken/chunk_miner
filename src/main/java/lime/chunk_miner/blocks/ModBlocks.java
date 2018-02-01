package lime.chunk_miner.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public class ModBlocks {
    public static Block dumb_miner_block;
    public static void init() {
        dumb_miner_block = new ChunkMinerBlock();
        GameRegistry.registerBlock(dumb_miner_block, "chunk_miner_block");

    }
}
