package lime.chunk_miner.tiles;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModTiles {
    public static final void init() {
        GameRegistry.registerTileEntity(ChunkMinerTile.class, "chunk_miner_tile");
    }

}
