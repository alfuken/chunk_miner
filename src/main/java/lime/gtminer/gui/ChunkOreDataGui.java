package lime.gtminer.gui;

import lime.gtminer.GtMiner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class ChunkOreDataGui extends Gui {
    public ChunkOreDataGui(Minecraft mc){
        int offset = 0;
        for (String s : GtMiner.get_current_chunk_cache_data()) {
            drawString(mc.fontRenderer, s, 5, 5+offset, Integer.parseInt("FFFFFF", 16));
            offset += 10;
        }
    }
}
