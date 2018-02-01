package lime.chunk_miner.items;

import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.gui.OreListPane;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import static lime.chunk_miner.ChunkMinerHelpers.loadScanDataOil;
import static lime.chunk_miner.ChunkMinerHelpers.loadScanDataOre;

public class ScanRegistry extends Item {
    public ScanRegistry(){
        setCreativeTab(ChunkMiner.ctab);
        setUnlocalizedName(ChunkMiner.MODID + ".scan_registry");
        setTextureName(ChunkMiner.MODID + ".scan_registry");
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer player) {
        if (w.isRemote){return itemStack;}

        new lime.chunk_miner.gui.OreListPane(player, loadScanDataOre(player), loadScanDataOil(player)).show();

        return itemStack;
    }

}
