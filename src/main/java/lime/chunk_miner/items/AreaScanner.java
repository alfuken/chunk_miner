package lime.chunk_miner.items;

import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.Utils;
import lime.chunk_miner.Config;
import lime.chunk_miner.network.SaveChunkScanReportMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.util.List;


public class AreaScanner extends Item {
    public AreaScanner() {
        setCreativeTab(ChunkMiner.ctab);
        setUnlocalizedName(ChunkMiner.MODID + ".area_scanner");
        setTextureName(ChunkMiner.MODID + ".area_scanner");
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer player) {
        if (w.isRemote) return itemStack;

        List<NBTTagCompound> data = Utils.areaScanResultsAsNBTList(w, (int)player.posX, (int)player.posZ, Config.area_scan_radius);

        NBTTagCompound all_data = new NBTTagCompound();
        all_data.setBoolean("big_batch", true);
        NBTTagList tag_list = new NBTTagList();
        for(NBTTagCompound entry : data){
            tag_list.appendTag(entry);
        }
        all_data.setTag("tag_list", tag_list);

        ChunkMiner.network.sendTo(new SaveChunkScanReportMessage(all_data), (EntityPlayerMP) player);

        if (!player.capabilities.isCreativeMode) --itemStack.stackSize;

        w.playSoundAtEntity(player, "IC2:Tools.ODScanner", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        return itemStack;
    }

}
