package lime.gtminer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lime.gtminer.gui.ChunkOreDataGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class RenderGuiHandler
{
    @SubscribeEvent
    public void onRenderGui(RenderGameOverlayEvent.Post event)
    {
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) return;
        new ChunkOreDataGui(Minecraft.getMinecraft());
    }
}
