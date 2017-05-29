package lime.dumb_miner.items;

import cpw.mods.fml.common.Loader;
import lime.dumb_miner.Config;
import lime.dumb_miner.DumbMiner;
import lime.dumb_miner.DumbMinerHelpers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import static lime.dumb_miner.DumbMinerHelpers.chunkScanReportAsString;

public class DumbScanner extends Item {
    public DumbScanner() {
        setCreativeTab(DumbMiner.ctab);
        setUnlocalizedName(DumbMiner.MODID + "_ore_scanner");
        setTextureName(DumbMiner.MODID + "_ore_scanner");
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer p) {
        if (w.isRemote){return itemStack;}

        if (Loader.isModLoaded("gregtech") && Config.inform_gt_chunks && DumbMinerHelpers.isGtChunk((int)p.posX, (int)p.posZ)){
            p.addChatMessage(new ChatComponentText("GT Vein chunk detected."));
        }

        p.addChatMessage(new ChatComponentText(chunkScanReportAsString(w, p)));

        if (!p.capabilities.isCreativeMode) --itemStack.stackSize;

        w.playSoundAtEntity(p, "IC2:tools.ODScanner", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        return itemStack;
    }
}
