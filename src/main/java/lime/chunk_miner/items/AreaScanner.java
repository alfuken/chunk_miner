package lime.chunk_miner.items;

import cpw.mods.fml.common.Loader;
import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.ChunkMinerHelpers;
import lime.chunk_miner.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Map;


public class AreaScanner extends Item {
    public AreaScanner() {
        setCreativeTab(ChunkMiner.ctab);
        setUnlocalizedName(ChunkMiner.MODID + ".area_scanner");
        setTextureName(ChunkMiner.MODID + ".area_scanner");
        setMaxDamage(250);
        setMaxStackSize(1);
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer player) {
        if (w.isRemote){return itemStack;}

        AreaScanner.scanAndSaveData(itemStack, w, player, 8);
        itemStack.damageItem(9, player);

        w.playSoundAtEntity(player, "IC2:tools.ODScanner", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        return itemStack;
    }

    public static void scanAndSaveData(ItemStack itemStack, World w, EntityPlayer player){
        scanAndSaveData(itemStack, w, player, 2);
    }

    public static void scanAndSaveData(ItemStack itemStack, World w, EntityPlayer player, int radius){
        ChunkMinerHelpers.saveScanData(player, "ore_scan_data", ChunkMinerHelpers.areaScanReportOre(w, (int)player.posX, (int)player.posZ, radius));

        if (Loader.isModLoaded("gregtech")) {
            ChunkMinerHelpers.saveScanData(player, "oil_scan_data", ChunkMinerHelpers.areaScanReportOil(w, (int) player.posX, (int) player.posZ, radius));
        }

        itemStack.damageItem(1, player);
    }

}
