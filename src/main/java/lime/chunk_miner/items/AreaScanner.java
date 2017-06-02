package lime.chunk_miner.items;

import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.ChunkMinerHelpers;
import lime.chunk_miner.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;


public class AreaScanner extends Item {
    public AreaScanner() {
        setCreativeTab(ChunkMiner.ctab);
        setUnlocalizedName(ChunkMiner.MODID + ".area_scanner");
        setTextureName(ChunkMiner.MODID + ".area_scanner");
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer p) {
        if (w.isRemote){return itemStack;}
        int range = Config.area_scan_radius;

        if (itemStack.getDisplayName().startsWith("range")){
            range = 8;
        }

        Map<String, List<String>> report = ChunkMinerHelpers.areaScanReport(w, p, range);
        int[] saving_result = ChunkMinerHelpers.saveScanData(p, report);
        p.addChatMessage(new ChatComponentText("Scanned "+saving_result[0]+" chunks, "+saving_result[1]+" new chunks added to Registry."));

        if (!p.capabilities.isCreativeMode) --itemStack.stackSize;

        w.playSoundAtEntity(p, "IC2:tools.ODScanner", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        return itemStack;
    }

}
