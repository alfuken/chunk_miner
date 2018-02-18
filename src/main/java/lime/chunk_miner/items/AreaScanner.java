package lime.chunk_miner.items;

import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.Config;
import lime.chunk_miner.Utils;
import lime.chunk_miner.network.SaveScanReportMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;


public class AreaScanner extends Item {
    public AreaScanner() {
        setCreativeTab(ChunkMiner.ctab);
        setUnlocalizedName(ChunkMiner.MODID + ".area_scanner");
        setTextureName(ChunkMiner.MODID + ".area_scanner");
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer p) {
        if (w.isRemote) return itemStack;

        NBTTagList data = Utils.scan(w, (int)p.posX, (int)p.posZ, Config.area_scan_radius);
        System.out.println("sending area scan="+data);

        ChunkMiner.network.sendTo(new SaveScanReportMessage(data), (EntityPlayerMP) p);

        if (!p.capabilities.isCreativeMode) --itemStack.stackSize;

        IC2.audioManager.playOnce(p, PositionSpec.Hand, "Tools/ODScanner.ogg", true, IC2.audioManager.getDefaultVolume());
//        w.playSoundAtEntity(p, "IC2:tools.ODScanner", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        return itemStack;
    }

}
