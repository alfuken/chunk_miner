package lime.chunk_miner.items;

import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.ChunkMinerHelpers;
import lime.chunk_miner.Config;
import lime.chunk_miner.gui.ScanResultsPane;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;

import java.util.*;


public class AreaScanner extends Item {
    public AreaScanner() {
        setCreativeTab(ChunkMiner.ctab);
        setUnlocalizedName(ChunkMiner.MODID + ".area_scanner");
        setTextureName(ChunkMiner.MODID + ".area_scanner");
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer p) {
        if (w.isRemote){return itemStack;}

        List<String> ddd = new ArrayList<String>();
        ddd.add("Yellow Limonite");
        ddd.add("Brown Limonite");
        ddd.add("Magnetite");
        new ScanResultsPane(ddd).show();

        int range = Config.area_scan_radius;

        if (itemStack.getDisplayName().startsWith("range")){
            range = 8;
        }

//        saveScanData(p, ChunkMinerHelpers.areaScanReport(w, p, range));
//        generateBook(p, scanReportAsPages(loadScanData(p)));

        if (!p.capabilities.isCreativeMode) --itemStack.stackSize;

        w.playSoundAtEntity(p, "IC2:tools.ODScanner", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        return itemStack;
    }

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

    private Map<String, List<String>> loadScanData(EntityPlayer player) {
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

    private void saveScanData(EntityPlayer player, Map<String, List<String>> scan_results){
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
                // we check if it's already stored and if not, store
                if (!coords_list_for_ore.contains(pair)){
                    coords_tags_for_ore.appendTag(new NBTTagString(pair));
                }
            }

            stored_scan_results.setTag(ore_name, coords_tags_for_ore);
        }

        player.getEntityData().setTag("stored_scan_results", stored_scan_results);
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

}
