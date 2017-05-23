package lime.gtminer;

import cpw.mods.fml.common.registry.GameRegistry;
import lime.gtminer.items.GtOreFinder;
import net.minecraft.item.Item;

public final class ModItems {
    public static Item gtOreFinder;

    public static final void init() {
        gtOreFinder = new GtOreFinder();
        GameRegistry.registerItem(gtOreFinder, "gtOreFinder");
    }

}
