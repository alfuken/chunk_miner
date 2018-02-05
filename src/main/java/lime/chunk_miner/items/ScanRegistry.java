package lime.chunk_miner.items;

import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.network.OpenScanRegistryMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ScanRegistry extends Item {
    public ScanRegistry(){
        setCreativeTab(ChunkMiner.ctab);
        setUnlocalizedName(ChunkMiner.MODID + ".scan_registry");
        setTextureName(ChunkMiner.MODID + ".scan_registry");
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer player) {
        if (w.isRemote) return itemStack;

        ChunkMiner.network.sendTo(new OpenScanRegistryMessage(), (EntityPlayerMP) player);

        return itemStack;
    }

}
