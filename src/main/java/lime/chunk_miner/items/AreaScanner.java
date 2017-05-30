package lime.chunk_miner.items;

import lime.chunk_miner.ChunkMiner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AreaScanner extends Item {
    public AreaScanner() {
        setCreativeTab(ChunkMiner.ctab);
        setUnlocalizedName(ChunkMiner.MODID + ".area_scanner");
        setTextureName(ChunkMiner.MODID + ".area_scanner");
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer p) {
        if (w.isRemote){return itemStack;}
//        p.addChatMessage(new ChatComponentText(areaScanReportAsString(w, p)));

        if (!p.capabilities.isCreativeMode) --itemStack.stackSize;

        return itemStack;
    }

}
