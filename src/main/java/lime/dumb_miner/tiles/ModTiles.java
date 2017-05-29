package lime.dumb_miner.tiles;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModTiles {
    public static final void init() {
        GameRegistry.registerTileEntity(DumbMinerTile.class, "dumb_miner_tile");
    }

}
