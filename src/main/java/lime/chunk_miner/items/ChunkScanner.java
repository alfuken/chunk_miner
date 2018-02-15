package lime.chunk_miner.items;

import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.ChunkMinerHelpers;
import lime.chunk_miner.network.SaveChunkScanReportMessage;
import lime.chunk_miner.network.ScanReportMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;


public class ChunkScanner extends Item {
    public ChunkScanner() {
        setCreativeTab(ChunkMiner.ctab);
        setUnlocalizedName(ChunkMiner.MODID + ".chunk_scanner");
        setTextureName(ChunkMiner.MODID + ".chunk_scanner");
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer p) {
        if (w.isRemote) return itemStack;

        Chunk c = w.getChunkFromBlockCoords((int)p.posX, (int)p.posZ);
        NBTTagCompound data = ChunkMinerHelpers.chunkScanResultsAsTag(c);
        ChunkMiner.network.sendTo(new SaveChunkScanReportMessage(data), (EntityPlayerMP) p);
        ChunkMiner.network.sendTo(new ScanReportMessage(data), (EntityPlayerMP) p);


        if (!p.capabilities.isCreativeMode) --itemStack.stackSize;

        w.playSoundAtEntity(p, "IC2:Tools.ODScanner", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        return itemStack;
    }
}
