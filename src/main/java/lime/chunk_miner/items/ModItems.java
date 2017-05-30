package lime.chunk_miner.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class ModItems {
    public static Item chunk_scanner;
    public static Item area_scanner;

    public static final void init() {
        chunk_scanner = new ChunkScanner();
        GameRegistry.registerItem(chunk_scanner, "chunk_scanner");

        area_scanner = new AreaScanner();
        GameRegistry.registerItem(area_scanner, "area_scanner");
    }
}
