package lime.gtminer.events;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lime.gtminer.GtMiner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;

public class EnteringChunkEventHandler {
    @SubscribeEvent(priority= EventPriority.NORMAL)
    public void onEvent(EntityEvent.EnteringChunk event){
        if (event.entity instanceof EntityPlayer){
            GtMiner.updateCurrentChunkCacheData(event.newChunkX+":"+event.newChunkZ);
        }
    }
}
