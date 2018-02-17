package lime.chunk_miner.items;

import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.Config;
import lime.chunk_miner.ScanDB;
import lime.chunk_miner.Utils;
import lime.chunk_miner.network.SaveChunkScanReportMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.HashMap;


public class AreaScanner extends Item {
    public AreaScanner() {
        setCreativeTab(ChunkMiner.ctab);
        setUnlocalizedName(ChunkMiner.MODID + ".area_scanner");
        setTextureName(ChunkMiner.MODID + ".area_scanner");
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer p) {
        if (w.isRemote) return itemStack;

        HashMap scan_result = Utils.scan(w, (int)p.posX, (int)p.posZ, Config.area_scan_radius);
        String data = Utils.mapToString(scan_result);
        System.out.println("Sending to "+p.getDisplayName()+" string of size "+data.length()+" bytes");
        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("payload", data);
        ChunkMiner.network.sendTo(new SaveChunkScanReportMessage(payload), (EntityPlayerMP) p);

        if (!p.capabilities.isCreativeMode) --itemStack.stackSize;

        w.playSoundAtEntity(p, "IC2:Tools.ODScanner", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        return itemStack;
    }

}
