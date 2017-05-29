package lime.dumb_miner.items;

import lime.dumb_miner.DumbMiner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AreaScanner extends Item {
    public AreaScanner() {
        setCreativeTab(DumbMiner.ctab);
        setUnlocalizedName(DumbMiner.MODID + "_area_scanner");
        setTextureName(DumbMiner.MODID + "_area_scanner");
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer p) {
        if (w.isRemote){return itemStack;}
//        p.addChatMessage(new ChatComponentText(areaScanReportAsString(w, p)));

        if (!p.capabilities.isCreativeMode) --itemStack.stackSize;

        return itemStack;
    }

}
