package lime.dumb_miner;

import cpw.mods.fml.common.Loader;
import gregtech.api.util.GT_Utility;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import net.minecraft.block.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class DumbMinerHelpers {

    public static boolean isGtChunk(int x, int z){
        return ((x / 16 - 1) % 3 == 0) && ((z / 16 - 1) % 3 == 0);
    }

    public static boolean shouldMine(World w, int x, int y, int z) {
        return shouldMine(w, x, y, z, w.getBlock(x, y, z));
    }

    public static boolean shouldMine(World w, int x, int y, int z, Block block){
        if (Config.scan_mode == "optimistic"){
            return (potentiallyValuableBlock(block));
        } else if (Config.scan_mode == "classic") {
            if (trashBlock(block)) return false;
            return (isValuable(new ItemStack(block, 1, w.getBlockMetadata(x, y, z))));
        } else {
            return true;
        }
    }

    public static boolean isValuable(World w, int x, int y, int z){
        return isValuable(new ItemStack(w.getBlock(x, y, z), 1, w.getBlockMetadata(x,y,z)));
    }

    public static boolean isValuable(ItemStack stack){
        for (Map.Entry<IRecipeInput, Integer> entry : IC2.valuableOres.entrySet()) {
            if (((IRecipeInput)entry.getKey()).matches(stack)) {
                return true;
            }
        }

        return false;
    }

    public static boolean potentiallyValuableBlock2(Block block){
        String n = block.getUnlocalizedName();
        return (n.equals("gt.blockores") ||
                n.startsWith("tile.ore") ||
                n.startsWith("blockOre")
        );
    }

    public static boolean potentiallyValuableBlock(Block block){
        return (block.getUnlocalizedName().contains("ore"));
    }

    public static List<String> chunkScanReportAsList(World w, EntityPlayer p){
        Map<String, Integer> ores = scan(w, (int)p.posX, (int)p.posZ);

        ArrayList<String> rows = new ArrayList<String>();
        rows.add("Resources at (" + (int) p.posX + ":" + (int) p.posZ + ")");
        for (Map.Entry<String, Integer> entry : ores.entrySet()) {
            rows.add(entry.getValue()+" "+entry.getKey());
        }

        if (Loader.isModLoaded("gregtech")){
            FluidStack fluid = GT_Utility.getUndergroundOil(w, (int)p.posX, (int)p.posZ);
            if (fluid.amount > 0) rows.add(fluid.amount/1000+" "+fluid.getLocalizedName());
        }

        return rows;
    }

    public static String chunkScanReportAsString(World w, EntityPlayer p){
        return StringUtils.join(chunkScanReportAsList(w, p), " - ");
    }

    private static boolean trashBlock(Block block){
        return (block == null
             || block instanceof BlockStone
             || block instanceof BlockAir
             || block instanceof IFluidBlock
             || block instanceof BlockStaticLiquid
             || block instanceof BlockDynamicLiquid
             || block instanceof BlockDirt
             || block instanceof BlockSand
             || block instanceof BlockSandStone
             || block instanceof BlockGravel
             || block == Blocks.bedrock
             || block == Blocks.cobblestone
             || block == Blocks.gravel
             || block == Blocks.obsidian
        );
    }

    public static Map<String, Integer> scan(World w, int wx, int wz) {
        return scan(w, wx, wz, 1);
    }

    public static Map<String, Integer> scan(World w, int wx, int wz, int radius) {
        long startTime = System.currentTimeMillis();

        Map<String, Integer> ret = new HashMap();
        int meta = 0;

        Chunk c = w.getChunkFromBlockCoords(wx, wz);

        for (int y = c.getTopFilledSegment() + 16; y > 0; y--) {
            for (int x = (c.xPosition * 16)-(16 * (radius - 1)); x < c.xPosition * 16 + (16 * radius); x++) {
                for (int z = (c.zPosition * 16)-(16 * (radius - 1)); z < c.zPosition * 16 + (16 * radius); z++) {
                    Block block = w.getBlock(x, y, z);

                    if (Config.scan_mode == "optimistic"){
                        if (!potentiallyValuableBlock(block)) continue;
                        meta = w.getBlockMetadata(x, y, z);
                    } else if (Config.scan_mode == "classic") {
                        if (trashBlock(block)) continue;
                        meta = w.getBlockMetadata(x, y, z);
                        if (!isValuable(new ItemStack(block, 1, meta))) continue;
                    } else {
                        meta = w.getBlockMetadata(x, y, z);
                    }

                    for (ItemStack drop : block.getDrops(w, x, y, z, meta, 0)) {
                        String key = drop.getItem().getItemStackDisplayName(drop);
                        boolean ignore_this = false;
                        for(String m : Config.ignored_materials){
                            if (key.contains(m)) ignore_this = true;
                        }

                        if (Config.skip_poor_ores && ignore_this) continue;

                        Integer count = ret.get(key);
                        if (count == null) count = 0;
                        count += 1;
                        ret.put(key, count);
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        System.out.println("\n===> Scan of "+wx+":"+wz+" took "+duration+" milliseconds");

        return sortByValue(ret);
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
}
