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

    public static List<String> scanReportAsPages(Map<String, List<String>> results) {
        ArrayList<String> rows = new ArrayList<String>();
        SortedSet<String> keys = new TreeSet<String>(results.keySet());
        for (String key : keys) {
            rows.add(key + "\n" + StringUtils.join(results.get(key), ", ") + "\n");
        }

        List<String> pages = new ArrayList<String>();
        pages.add("");

        for (String row : rows) {
            String last_page = pages.get(pages.size()-1);
            if (last_page.length() + row.length() < 253){
                last_page += row+"\n";
                pages.set(pages.size()-1, last_page);
            } else {
                String new_page = row+"\n";
                pages.add(new_page);
            }
        }

        return pages;
    }

    public static Map<String, List<String>> loadScanData(EntityPlayer player) {
        NBTTagCompound player_root_tag = player.getEntityData();
        NBTTagCompound stored_scan_results = player_root_tag.getCompoundTag("stored_scan_results");
        if (stored_scan_results == null) stored_scan_results = new NBTTagCompound();

        Map<String, List<String>> results = new HashMap<String, List<String>>();

        SortedSet<String> keys = new TreeSet<String>();
        for (Object _ore_name : stored_scan_results.func_150296_c()) keys.add((String) _ore_name);
        for (String ore_name : keys) {
            NBTTagList coords_tags_for_ore = stored_scan_results.getTagList(ore_name, 8);

            SortedSet<String> coords_list_for_ore = new TreeSet<String>();

            for (int i = 0; i < coords_tags_for_ore.tagCount(); i++) {
                coords_list_for_ore.add(coords_tags_for_ore.getStringTagAt(i));
            }

            results.put(ore_name, new ArrayList<String>(coords_list_for_ore));
        }

        return results;
    }

    public static int[] saveScanData(EntityPlayer player, Map<String, List<String>> scan_results){
        int scanned = 0;
        int added = 0;
        NBTTagCompound player_root_tag = player.getEntityData();
        NBTTagCompound stored_scan_results = player_root_tag.getCompoundTag("stored_scan_results");
        if (stored_scan_results == null) stored_scan_results = new NBTTagCompound();

        SortedSet<String> ore_names = new TreeSet<String>(scan_results.keySet());
        for (String ore_name : ore_names) {

            if (!stored_scan_results.hasKey(ore_name)) stored_scan_results.setTag(ore_name, new NBTTagList());

            NBTTagList coords_tags_for_ore = stored_scan_results.getTagList(ore_name, 8);

            List<String> coords_list_for_ore = new ArrayList<String>();

            // first we collect already stored coord pairs
            for (int i = 0; i < coords_tags_for_ore.tagCount(); i++) {
                coords_list_for_ore.add(coords_tags_for_ore.getStringTagAt(i));
            }

            // then for each new pair
            for (String pair : scan_results.get(ore_name)){
                scanned++;
                // we check if it's already stored and if not, store
                if (!coords_list_for_ore.contains(pair)){
                    added++;
                    coords_tags_for_ore.appendTag(new NBTTagString(pair));
                }
            }

            stored_scan_results.setTag(ore_name, coords_tags_for_ore);
        }

        player.getEntityData().setTag("stored_scan_results", stored_scan_results);
        return new int[]{scanned, added};
    }

    public void generateBook(EntityPlayer player, List<String> pages){
        ItemStack book = new ItemStack(Items.written_book);

        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList bookPages = new NBTTagList();
        for(String page : pages){
            bookPages.appendTag(new NBTTagString(page));
        }
        book.setTagInfo("pages", bookPages);
        book.setTagInfo("author", new NBTTagString(player.getDisplayName()));
        book.setTagInfo("title", new NBTTagString("Ore scans"));

        // Give the player the book
        player.inventory.addItemStackToInventory(book);
    }

    public static boolean isGtChunk(int x, int z){
        return ((x / 16 - 1) % 3 == 0) && ((z / 16 - 1) % 3 == 0);
    }

    public static boolean shouldMine(World w, int x, int y, int z){
        if (y <= 0 || y >= 255) return false;

        Block block = w.getBlock(x, y, z);

        if (block.getBlockHardness(w, x, y, z) < 0.0F) return false;

        if (Config.scan_mode.equals("optimistic")){
            return (potentiallyValuableBlock(block));
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


    public static Map<String, List<String>> areaScanReport(World w, EntityPlayer p, int radius) {
        long startTime = System.currentTimeMillis();

        Map<String, List<String>> results = new HashMap();
        int cx = (int)p.posX / 16;
        int cz = (int)p.posZ / 16;

        for (int x = cx-radius; x <= cx+radius; x++) {
            for (int z = cz-radius; z <= cz+radius; z++) {
                try {
                    Map<String, Integer> ores = scan(w, (x*16)+8, (z*16)+8);
                    Map.Entry<String, Integer> entry = ores.entrySet().iterator().next();
                    String key = entry.getKey();

                    List<String> coords_list = results.get(key);
                    if (coords_list == null) coords_list = new ArrayList<String>();
                    coords_list.add(((x*16)+8)+":"+((z*16)+8));
                    results.put(key, coords_list);
                } catch (NoSuchElementException nsee){}
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        System.out.println("\n===> Area scan at "+cx+":"+cz+" took "+duration+" milliseconds");

        return results;
    }

    public static String chunkScanReportAsString(World w, EntityPlayer p){
        return StringUtils.join(chunkScanReportAsList(w, p), " - ");
    }

    public static boolean trashBlock(Block block){
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

                    if (Config.scan_mode.equals("optimistic")){
                        if (!potentiallyValuableBlock(block)) continue;
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
