package lime.dumb_miner.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class ModItems {
    public static Item scanner;
    public static Item area_scanner;

    public static final void init() {
        scanner = new DumbScanner();
        GameRegistry.registerItem(scanner, "dumb_scanner");

//        area_scanner = new AreaScanner();
//        GameRegistry.registerItem(area_scanner, "area_scanner");
    }
}
