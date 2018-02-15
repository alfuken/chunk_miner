package lime.chunk_miner.items;

import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.ChunkMinerHelpers;
import lime.chunk_miner.Config;
import lime.chunk_miner.network.SaveChunkScanReportMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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

        List<NBTTagCompound> data = ChunkMinerHelpers.areaScanResultsAsNBTList(w, (int)player.posX, (int)player.posZ, Config.area_scan_radius);
        for(NBTTagCompound entry : data){
            ChunkMiner.network.sendTo(new SaveChunkScanReportMessage(entry), (EntityPlayerMP) player);
        }

        if (!player.capabilities.isCreativeMode) --itemStack.stackSize;

        w.playSoundAtEntity(player, "IC2:tools.ODScanner", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        return itemStack;
    }

}
