package lime.chunk_miner;

import cpw.mods.fml.common.Loader;
import gregtech.api.util.GT_Utility;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import net.minecraft.block.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ChunkMinerHelpers {

    public static Map<String, String> areaScanReportOre(World w, int wx, int wz, int radius) {
        return areaScanReport(w, wx, wz, radius, true);
    }

    public static Map<String, String> areaScanReportOil(World w, int wx, int wz, int radius) {
        return areaScanReport(w, wx, wz, radius, false);
    }

    public static Map<String, String> areaScanReport(World w, int wx, int wz, int radius, boolean ore) {
        long startTime = System.currentTimeMillis();

        Map<String, String> coord_list = new HashMap<String, String>();
        int cx = wx / 16;
        int cz = wz / 16;

        for (int x = cx-radius; x <= cx+radius; x++) {
            for (int z = cz-radius; z <= cz+radius; z++) {
                if (ore){
                    try {
                        Map<String, Integer> ores = scan(w, (x*16)+8, (z*16)+8);
                        Map.Entry<String, Integer> most_common = ores.entrySet().iterator().next();
                        String ore_name = most_common.getKey();
                        coord_list.put(x+":"+z, ore_name);
                    } catch (NoSuchElementException e){}
                } else { // oil
                    if (Loader.isModLoaded("gregtech")){
                        FluidStack fluid = GT_Utility.getUndergroundOil(w, (x*16)+8, (z*16)+8);
                        if (fluid.amount >= 5000) coord_list.put(x+":"+z, fluid.amount/1000+" x "+fluid.getLocalizedName());
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        System.out.println("\n===> areaScanReport2(w, "+cx+", "+cz+", "+radius+", "+ore+") took "+duration+"ms");

        return coord_list;
    }


    public static void saveScanData(EntityPlayer player, String storage_key, Map<String, String> scan_data){
        NBTTagCompound stored_scan_data = player.getEntityData().getCompoundTag(storage_key);
        if (stored_scan_data == null) stored_scan_data = new NBTTagCompound();

        for (String coord : scan_data.keySet()) {
            stored_scan_data.setTag(coord, new NBTTagString(scan_data.get(coord)));
        }

        player.getEntityData().setTag(storage_key, stored_scan_data);
    }

    public static Map<String, String> loadScanDataOre(EntityPlayer player) {
        return loadScanData(player, "ore_scan_data");
    }

    public static Map<String, String> loadScanDataOil(EntityPlayer player) {
        return loadScanData(player, "oil_scan_data");
    }

    public static Map<String, String> loadScanData(EntityPlayer player, String storage_key) {
        NBTTagCompound stored_scan_data = player.getEntityData().getCompoundTag(storage_key);
        if (stored_scan_data == null) stored_scan_data = new NBTTagCompound();

        Map<String, String> results = new HashMap<String, String>();

        for (Object _coord : stored_scan_data.func_150296_c()) {
            String coord = (String)_coord;
            results.put(coord, stored_scan_data.getString(coord));
        }

        return results;
    }

    public static String[] getScanDataNames(Map<String, String> data){
        Collection<String> vals = data.values();
        return new TreeSet<String>(vals).toArray(new String[vals.size()]);
    }

    public static String[] getScanDataCoordsByName(Map<String, String> data, String name){
        List<String> coords = new ArrayList<String>();
        for (Map.Entry<String, String> entry : data.entrySet()){
            if (entry.getValue().equals(name)) coords.add(entry.getKey());
        }
        return coords.toArray(new String[coords.size()]);
    }

//    public static Map<String, List<String>> loadScanData(EntityPlayer player) {
//        NBTTagCompound player_root_tag = player.getEntityData();
//        NBTTagCompound stored_scan_results = player_root_tag.getCompoundTag("stored_scan_results");
//        if (stored_scan_results == null) stored_scan_results = new NBTTagCompound();
//
//        Map<String, List<String>> results = new HashMap<String, List<String>>();
//
//        SortedSet<String> keys = new TreeSet<String>();
//        for (Object _ore_name : stored_scan_results.func_150296_c()) keys.add((String) _ore_name);
//        for (String ore_name : keys) {
//            NBTTagList coords_tags_for_ore = stored_scan_results.getTagList(ore_name, 8);
//
//            SortedSet<String> coords_list_for_ore = new TreeSet<String>();
//
//            for (int i = 0; i < coords_tags_for_ore.tagCount(); i++) {
//                coords_list_for_ore.add(coords_tags_for_ore.getStringTagAt(i));
//            }
//
//            results.put(ore_name, new ArrayList<String>(coords_list_for_ore));
//        }
//
//        return results;
//    }
//
//    public static int[] saveScanData(EntityPlayer player, Map<String, List<String>> scan_results){
//        int scanned = 0;
//        int added = 0;
//        NBTTagCompound player_root_tag = player.getEntityData();
//        NBTTagCompound stored_scan_results = player_root_tag.getCompoundTag("stored_scan_results");
//        if (stored_scan_results == null) stored_scan_results = new NBTTagCompound();
//
//        SortedSet<String> ore_names = new TreeSet<String>(scan_results.keySet());
//        for (String ore_name : ore_names) {
//
//            if (!stored_scan_results.hasKey(ore_name)) stored_scan_results.setTag(ore_name, new NBTTagList());
//
//            NBTTagList coords_tags_for_ore = stored_scan_results.getTagList(ore_name, 8);
//
//            List<String> coords_list_for_ore = new ArrayList<String>();
//
//            // first we collect already stored coord pairs
//            for (int i = 0; i < coords_tags_for_ore.tagCount(); i++) {
//                coords_list_for_ore.add(coords_tags_for_ore.getStringTagAt(i));
//            }
//
//            // then for each new pair
//            for (String pair : scan_results.get(ore_name)){
//                scanned++;
//                // we check if it's already stored and if not, store
//                if (!coords_list_for_ore.contains(pair)){
//                    added++;
//                    coords_tags_for_ore.appendTag(new NBTTagString(pair));
//                }
//            }
//
//            stored_scan_results.setTag(ore_name, coords_tags_for_ore);
//        }
//
//        player.getEntityData().setTag("stored_scan_results", stored_scan_results);
//        return new int[]{scanned, added};
//    }

    public static boolean isGtChunk(int x, int z){
        return ((x / 16 - 1) % 3 == 0) && ((z / 16 - 1) % 3 == 0);
    }

    public static boolean shouldMine(World w, int x, int y, int z){
        if (y <= 0 || y >= 255) return false;

        Block block = w.getBlock(x, y, z);

        if (block.getBlockHardness(w, x, y, z) < 0.0F) return false;

        if (Config.scan_mode.equals("optimistic")){
            return (potentiallyValuableBlock(block));
        } else if (Config.scan_mode.equals("pessimistic")){
            return (potentiallyValuableBlock2(block));
        } else if (Config.scan_mode.equals("classic")) {
            if (trashBlock(block)) return false;
            return (isValuable(new ItemStack(block, 1, w.getBlockMetadata(x, y, z))));
        } else {
            return (!trashBlock(block));
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


//    public static Map<String, List<String>> areaScanReport(World w, EntityPlayer p, int radius) {
//        long startTime = System.currentTimeMillis();
//
//        Map<String, List<String>> results = new HashMap();
//        int cx = (int)p.posX / 16;
//        int cz = (int)p.posZ / 16;
//
//        for (int x = cx-radius; x <= cx+radius; x++) {
//            for (int z = cz-radius; z <= cz+radius; z++) {
//                try {
//                    Map<String, Integer> ores = scan(w, (x*16)+8, (z*16)+8);
//                    Map.Entry<String, Integer> entry = ores.entrySet().iterator().next();
//                    String key = entry.getKey();
//
//                    List<String> coords_list = results.get(key);
//                    if (coords_list == null) coords_list = new ArrayList<String>();
//                    coords_list.add(((x*16)+8)+":"+((z*16)+8));
//                    results.put(key, coords_list);
//                } catch (NoSuchElementException nsee){}
//            }
//        }
//
//        long endTime = System.currentTimeMillis();
//        long duration = (endTime - startTime);
//        System.out.println("\n===> areaScanReport(w, "+cx+":"+cz+", "+radius+") took "+duration+"ms");
//
//        return results;
//    }

    public static String chunkScanReportAsString(World w, EntityPlayer p){
        return StringUtils.join(chunkScanReportAsList(w, p), " - ");
    }

    public static boolean trashBlock(Block block){
        return (block == null
             || block instanceof BlockStone
             || block instanceof BlockAir
             || block instanceof BlockDirt
             || block instanceof IFluidBlock
             || block instanceof BlockStaticLiquid
             || block instanceof BlockDynamicLiquid
             || block instanceof BlockSand
             || block instanceof BlockSandStone
             || block instanceof BlockGravel
//             || block.equals(Blocks.bedrock)
//             || block.equals(Blocks.cobblestone)
//             || block.equals(Blocks.gravel)
//             || block.equals(Blocks.obsidian)
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

                    if (Config.scan_mode.equals("optimistic")){
                        if (!potentiallyValuableBlock(block)) continue;
                        meta = w.getBlockMetadata(x, y, z);
                    } else if (Config.scan_mode.equals("pessimistic")){
                        if (!potentiallyValuableBlock2(block)) continue;
                        meta = w.getBlockMetadata(x, y, z);
                    } else if (Config.scan_mode.equals("classic")) {
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
        System.out.println("\n===> scan(w, "+wx+", "+wz+") took "+duration+"ms");

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
