package lime.dumb_miner;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lime.dumb_miner.gui.ChunkOreDataGui;
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
