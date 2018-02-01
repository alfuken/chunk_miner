package lime.chunk_miner.items;

import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.ChunkMinerHelpers;
import lime.chunk_miner.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AreaScanner extends Item {
    public AreaScanner() {
        setCreativeTab(ChunkMiner.ctab);
        setUnlocalizedName(ChunkMiner.MODID + ".area_scanner");
        setTextureName(ChunkMiner.MODID + ".area_scanner");
        setMaxDamage(200);
        setMaxStackSize(64);
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer player) {
        if (w.isRemote){return itemStack;}

        ChunkMinerHelpers.scanAndSaveData(itemStack, w, player, Config.area_scan_radius);
        itemStack.damageItem(67, player);

        w.playSoundAtEntity(player, "IC2:tools.ODScanner", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        return itemStack;
    }

}
