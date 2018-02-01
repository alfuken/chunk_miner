package lime.chunk_miner;

import cpw.mods.fml.common.Loader;
import gregtech.common.GT_UndergroundOil;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import lime.chunk_miner.blocks.ChunkMinerBlock;
import net.minecraft.block.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ChunkMinerHelpers {

    public static void scanAndSaveData(ItemStack itemStack, World w, EntityPlayer player){
        scanAndSaveData(itemStack, w, player, 0);
    }

    public static void scanAndSaveData(ItemStack itemStack, World w, EntityPlayer player, int radius){
//        saveScanData(player, "ore_scan_data", areaScanReportOre(w, (int)player.posX, (int)player.posZ, radius));

//        if (Loader.isModLoaded("gregtech")) {
//            saveScanData(player, "oil_scan_data", areaScanReportOil(w, (int) player.posX, (int) player.posZ, radius));
//        }

//        itemStack.damageItem(1, player);
    }

    public static void scanAndSaveData(World w, EntityPlayer player){
        scanAndSaveData(w, player, 0);
    }

    public static void scanAndSaveData(World w, EntityPlayer player, int radius){
//        saveScanData(player, "ore_scan_data", areaScanReportOre(w, (int)player.posX, (int)player.posZ, radius));
//
//        if (Loader.isModLoaded("gregtech")) {
//            saveScanData(player, "oil_scan_data", areaScanReportOil(w, (int) player.posX, (int) player.posZ, radius));
//        }
    }

    public static Map<String, String> areaScanReportOre(World w, int wx, int wz, int radius) {
        return areaScanReport(w, wx, wz, radius, true);
    }

    public static Map<String, String> areaScanReportOil(World w, int wx, int wz, int radius) {
        return areaScanReport(w, wx, wz, radius, false);
    }

    public static Map<String, String> areaScanReport(World w, int wx, int wz, int radius, boolean ore) {
//        long startTime = System.currentTimeMillis();

        Map<String, String> coord_list = new HashMap<String, String>();
        Chunk c = w.getChunkFromBlockCoords(wx, wz);

        for (int x = c.xPosition-radius; x <= c.xPosition+radius; x++) {
            for (int z = c.zPosition-radius; z <= c.zPosition+radius; z++) {
                if (ore){
                    try {
                        Map<String, Integer> ores = scanChunk(c);
                        Map.Entry<String, Integer> most_common = ores.entrySet().iterator().next();
                        String ore_name = most_common.getKey();
                        coord_list.put(x+":"+z, ore_name);
                    } catch (NoSuchElementException e){}
                } else { // oil
                    if (Loader.isModLoaded("gregtech")){
                        FluidStack fluid = GT_UndergroundOil.undergroundOil(w.getChunkFromChunkCoords(x, z), -1.0F);
                        if (fluid != null){
                            coord_list.put(x+":"+z, fluid.amount+" x "+fluid.getLocalizedName());
//                          if (fluid.amount >= 5000) coord_list.put(x+":"+z, fluid.amount/1000+" x "+fluid.getLocalizedName());
                        }
                    }
                }
            }
        }

//        long endTime = System.currentTimeMillis();
//        long duration = (endTime - startTime);
//        System.out.println("\n===> areaScanReport2(w, "+cx+", "+cz+", "+radius+", "+ore+") took "+duration+"ms");

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

    public static boolean isGtChunk(int x, int z){
        return ((x / 16 - 1) % 3 == 0) && ((z / 16 - 1) % 3 == 0);
    }

    public static boolean mineableNonStone(Block block){
        return(mineable(block) && !(block instanceof BlockStone));
    }

    public static boolean mineable(Block block){
        return(!(block instanceof BlockAir
              || block instanceof ChunkMinerBlock
              || block instanceof IFluidBlock
              || block instanceof BlockLiquid
              || block.equals(Blocks.bedrock)
        ));
    }

    public static boolean genericMineable(Block block){
        if (Config.scan_mode.equals("optimistic")){
            return (potentiallyValuableBlock(block));
        } else if (Config.scan_mode.equals("pessimistic")){
            return (potentiallyValuableBlock2(block));
        } else {
            return !trashBlock(block);
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
        Map<String, Integer> ores = scanChunk(w.getChunkFromBlockCoords((int)p.posX, (int)p.posZ));

        ArrayList<String> rows = new ArrayList<String>();
        rows.add("Resources at (" + (int) p.posX + ":" + (int) p.posZ + ")");
        for (Map.Entry<String, Integer> entry : ores.entrySet()) {
            rows.add(entry.getValue()+" "+entry.getKey());
        }

        if (Loader.isModLoaded("gregtech")){
            FluidStack fluid = GT_UndergroundOil.undergroundOil(w.getChunkFromChunkCoords(p.chunkCoordX, p.chunkCoordZ), -1.0F);
            if (fluid != null){
                rows.add(fluid.amount+" x "+fluid.getLocalizedName());
            }
//            FluidStack fluid = GT_Utility.getUndergroundOil(w, (int)p.posX, (int)p.posZ);
//            if (fluid.amount > 0) rows.add(fluid.amount/1000+" "+fluid.getLocalizedName());
        }

        return rows;
    }

    public static String chunkScanReportAsString(World w, EntityPlayer p){
        return StringUtils.join(chunkScanReportAsList(w, p), " - ");
    }

    public static boolean trashBlock(Block block){
        return !mineable(block) || (block == null
             || block instanceof BlockStone
             || block instanceof ChunkMinerBlock
             || block instanceof BlockAir
             || block instanceof BlockDirt
             || block instanceof IFluidBlock
             || block instanceof BlockLiquid
             || block instanceof BlockSand
             || block instanceof BlockSandStone
             || block instanceof BlockGravel
             || block.equals(Blocks.bedrock)
             || block.equals(Blocks.cobblestone)
             || block.equals(Blocks.gravel)
             || block.equals(Blocks.obsidian)
        );
    }

    public static boolean genericMineable(World w, int x, int y, int z){
        Block block = w.getBlock(x, y, z);
        if (Config.scan_mode.equals("optimistic")){
            return (potentiallyValuableBlock(block));
        } else if (Config.scan_mode.equals("pessimistic")){
            return (potentiallyValuableBlock2(block));
        } else if (Config.scan_mode.equals("classic")) {
            return (isValuable(w, x, y, z));
        } else {
            return !trashBlock(block);
        }
    }

    public static Map<String, Integer> scanChunk(Chunk c) {
        Map<String, Integer> scanResults = new HashMap();
        int meta;
        Block block;
        World w = c.worldObj;

        for (int y = c.getTopFilledSegment() + 16; y > 0; y--) {
            for (int x = (c.xPosition * 16); x < (c.xPosition * 16) + 16; x++) {
                for (int z = (c.zPosition * 16); z < (c.zPosition * 16) + 16; z++) {
                    if (!genericMineable(w, x, y, z)) continue;

                    block = w.getBlock(x, y, z);
                    meta = w.getBlockMetadata(x, y, z);
                    for (ItemStack drop : block.getDrops(w, x, y, z, meta, 0)) {
                        String key = drop.getItem().getItemStackDisplayName(drop);

                        if (skipPoory(key)) continue;

                        Integer count = scanResults.get(key);
                        if (count == null) count = 0;
                        count += 1;
                        scanResults.put(key, count);
                    }
                }
            }
        }

        return sortByValue(scanResults);

    }

    private static boolean skipPoory(String name){
        if (Config.skip_poor_ores) {
            for(String m : Config.ignored_materials){
                if (name.contains(m)) return true;
            }
        }
        return false;
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
