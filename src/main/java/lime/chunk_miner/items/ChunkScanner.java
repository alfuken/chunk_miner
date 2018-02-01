package lime.chunk_miner.items;

import cpw.mods.fml.common.Loader;
import lime.chunk_miner.Config;
import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.ChunkMinerHelpers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import static lime.chunk_miner.ChunkMinerHelpers.chunkScanReportAsString;

public class ChunkScanner extends Item {
    public ChunkScanner() {
        setCreativeTab(ChunkMiner.ctab);
        setUnlocalizedName(ChunkMiner.MODID + ".chunk_scanner");
        setTextureName(ChunkMiner.MODID + ".chunk_scanner");
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer p) {
        if (w.isRemote){
            w.playSoundAtEntity(p, "IC2:tools.ODScanner", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            return itemStack;
        }

//        if (Loader.isModLoaded("gregtech") && Config.inform_gt_chunks && ChunkMinerHelpers.isGtChunk((int)p.posX, (int)p.posZ)){
//            p.addChatMessage(new ChatComponentText("GT chunk detected."));
//        }

        ChunkMinerHelpers.scanAndSaveData(itemStack, w, p);

        p.addChatMessage(new ChatComponentText(chunkScanReportAsString(w, p)));

        if (!p.capabilities.isCreativeMode) --itemStack.stackSize;

        return itemStack;
    }
}
