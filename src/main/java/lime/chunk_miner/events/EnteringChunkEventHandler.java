package lime.chunk_miner.events;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lime.chunk_miner.items.AreaScanner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityEvent;

public class EnteringChunkEventHandler {
    @SubscribeEvent(priority= EventPriority.NORMAL)
    public void onEvent(EntityEvent.EnteringChunk event){
        if (event.entity instanceof EntityPlayer){
            EntityPlayer player = (EntityPlayer)event.entity;
            ItemStack itemStack = player.getCurrentEquippedItem();
            if (itemStack != null){
                Item item = itemStack.getItem();
                if (item instanceof AreaScanner && (item.getDamage(itemStack) > 0)){
                    AreaScanner.scanAndSaveData(itemStack, player.getEntityWorld(), player);
                }
            }
        }
    }
}
