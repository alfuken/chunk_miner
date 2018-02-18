package lime.chunk_miner.items;

import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.Config;
import lime.chunk_miner.Utils;
import lime.chunk_miner.network.SaveChunkScanReportMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
        byte[] data = Utils.mapToNBT(scan_result);
        System.out.println("Sending to "+p.getDisplayName()+" packet of size "+data.length+" bytes");

        ChunkMiner.network.sendTo(new SaveChunkScanReportMessage(data), (EntityPlayerMP) p);

        if (!p.capabilities.isCreativeMode) --itemStack.stackSize;

        IC2.audioManager.playOnce(p, PositionSpec.Hand, "Tools/ODScanner.ogg", true, IC2.audioManager.getDefaultVolume());
//        w.playSoundAtEntity(p, "IC2:tools.ODScanner", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        return itemStack;
    }

}
