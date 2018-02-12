package lime.chunk_miner;

import cpw.mods.fml.common.Loader;
import gregtech.common.GT_UndergroundOil;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import lime.chunk_miner.blocks.ChunkMinerBlock;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ChunkMinerHelpers {
    public static File playerFile(EntityPlayer player){
        return new File(ChunkMiner.MODID+File.separator+ Minecraft.getMinecraft()+player.getDisplayName()+".dat");
    }

    public static NBTTagCompound readPlayer(EntityPlayer player){
        NBTTagCompound tag = new NBTTagCompound();

        try {
            tag = CompressedStreamTools.read(playerFile(player));
        } catch (IOException io){
            io.printStackTrace();
        }

        return tag;
    }

    public static void writePlayer(EntityPlayer player, NBTTagCompound tag){
        File f = playerFile(player);

        if (!f.exists()){
            try {
                f.getParentFile().mkdirs();
                f.createNewFile();
            } catch (IOException io){
                io.printStackTrace();
            }
        }

        try {
            CompressedStreamTools.write(tag, f);
        } catch (IOException io){
            io.printStackTrace();
        }
    }

    public static void clearScanData(EntityPlayer player) {
        writePlayer(player, new NBTTagCompound());
    }

    public static void saveScanData(EntityPlayer player, NBTTagCompound scan_data){
        saveScanData(player, scanDataNBTToMap(scan_data));
    }

    public static void saveScanData(EntityPlayer player, Map<String, NBTTagCompound> scan_data){
        NBTTagCompound stored_scan_data = readPlayer(player);
        if (stored_scan_data == null) stored_scan_data = new NBTTagCompound();

        for (Map.Entry<String, NBTTagCompound> entry : scan_data.entrySet()){
            stored_scan_data.setTag(entry.getKey(), entry.getValue());
        }

        writePlayer(player, stored_scan_data);
    }

    public static NBTTagCompound scanDataMapToNBT(Map<String, NBTTagCompound> data){
        NBTTagCompound ret = new NBTTagCompound();
        for (Map.Entry<String, NBTTagCompound> entry : data.entrySet()){
            ret.setTag(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    public static Map<String, NBTTagCompound> scanDataNBTToMap(EntityPlayer player) {
        return scanDataNBTToMap(readPlayer(player));
    }

    public static Map<String, NBTTagCompound> scanDataNBTToMap(NBTTagCompound data) {
        String coord;

        Map<String, NBTTagCompound> ret = new HashMap<String, NBTTagCompound>();

        if (data == null) return ret;

        for (Object _coord : data.func_150296_c()) {
            coord = (String)_coord;
            ret.put(coord, data.getCompoundTag(coord));
        }

        return ret;
    }

    public static String[] getScanDataNames(Map<String, NBTTagCompound> data){
        Collection<String> vals = new ArrayList<String>();
        for(NBTTagCompound entry : data.values()){
            vals.add(ItemStack.loadItemStackFromNBT(entry.getCompoundTag("item")).getDisplayName());
        }
        return new TreeSet<String>(vals).toArray(new String[vals.size()]);
    }

    public static String[] getScanDataOilNames(Map<String, NBTTagCompound> data){
        Collection<String> vals = new ArrayList<String>();
        for(NBTTagCompound entry : data.values()){
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(entry.getCompoundTag("fluid"));
            if (fluid != null){
                vals.add(fluid.getLocalizedName());
            }
        }
        return new TreeSet<String>(vals).toArray(new String[vals.size()]);
    }

    public static List<NBTTagCompound> getScanDataByName(Map<String, NBTTagCompound> haystack, String needle){
        List<NBTTagCompound> coords = new ArrayList<NBTTagCompound>();
        for (Map.Entry<String, NBTTagCompound> entry : haystack.entrySet()){
            if (ItemStack.loadItemStackFromNBT(entry.getValue().getCompoundTag("item")).getDisplayName().equals(needle)){
                coords.add(entry.getValue());
            }
        }
        return coords;
    }

    public static String[] getScanDataCoordsByName(Map<String, NBTTagCompound> haystack, String needle){
        List<String> coords = new ArrayList<String>();
        for (Map.Entry<String, NBTTagCompound> entry : haystack.entrySet()){
            if (ItemStack.loadItemStackFromNBT(entry.getValue().getCompoundTag("item")).getDisplayName().equals(needle)){
                coords.add(entry.getKey());
            }
        }
        return coords.toArray(new String[coords.size()]);
    }

    public static String[] getScanDataCoordsByOilName(Map<String, NBTTagCompound> haystack, String needle){
        List<String> coords = new ArrayList<String>();
        for (Map.Entry<String, NBTTagCompound> entry : haystack.entrySet()){
            NBTTagCompound tag = entry.getValue();
            if (tag.hasKey("fluid")) {
                FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("fluid"));
                if (fluid != null && fluid.getLocalizedName().equals(needle)){
                    coords.add(entry.getKey());
                }
            }

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

    public static NBTTagCompound areaScanAsNBT(World w, int x, int z) {
        return scanDataMapToNBT(areaScanAsMap(w, x, z));
    }

    public static NBTTagCompound areaScanAsNBT(World w, int x, int z, int radius) {
        return scanDataMapToNBT(areaScanAsMap(w, x, z, radius));
    }

    public static Map<String, NBTTagCompound> areaScanAsMap(World w, int wx, int wz) {
        return areaScanAsMap(w, wx, wz, 0);
    }

    public static Map<String, NBTTagCompound> areaScanAsMap(World w, int wx, int wz, int radius) {
        Map<String, NBTTagCompound> coord_list = new HashMap<String, NBTTagCompound>();
        Chunk c = w.getChunkFromBlockCoords(wx, wz);
        Chunk chunk;
        NBTTagCompound entry;

        for (int x = c.xPosition-radius; x <= c.xPosition+radius; x++) {
            for (int z = c.zPosition-radius; z <= c.zPosition+radius; z++) {
                try {
                    chunk = w.getChunkFromChunkCoords(x, z);
                    Map.Entry<ItemStack, Integer> most_common = scanChunk(chunk).entrySet().iterator().next();
                    entry = packPairEntryToNBT(most_common);
                    addFluidInfoToTag(entry, w, x, z);
                    coord_list.put(x+":"+z, entry);
                } catch (NoSuchElementException e){}
            }
        }

        return coord_list;
    }

    private static NBTTagCompound addFluidInfoToTag(NBTTagCompound tag, World w, int x, int z){
        if (Loader.isModLoaded("gregtech")){
            FluidStack fluidStack = GT_UndergroundOil.undergroundOil(w.getChunkFromChunkCoords(x, z), -1.0F);
            if (fluidStack != null){
                tag.setInteger("fluid_amount", fluidStack.amount);
                NBTTagCompound fluid_tag = new NBTTagCompound();
                fluidStack.writeToNBT(fluid_tag);
                tag.setTag("fluid", fluid_tag);
            }
        }
        return tag;
    }

    private static NBTTagCompound packPairEntryToNBT(Map.Entry<ItemStack, Integer> entry){
        NBTTagCompound row = new NBTTagCompound();
        NBTTagCompound item = new NBTTagCompound();
        entry.getKey().writeToNBT(item);
        row.setTag("item", item);
        row.setInteger("count", entry.getValue());
        return row;
    }

    public static NBTTagCompound chunkScanAsNBT(World w, EntityPlayer p){
        NBTTagCompound ret = new NBTTagCompound();

        Map<ItemStack, Integer> ores = scanChunk(w.getChunkFromBlockCoords((int)p.posX, (int)p.posZ));

        NBTTagList ore_list = new NBTTagList();
        for (Map.Entry<ItemStack, Integer> entry : ores.entrySet()) {
            ore_list.appendTag(packPairEntryToNBT(entry));
        }
        ret.setTag("ore_list", ore_list);

        addFluidInfoToTag(ret, w, p.chunkCoordX, p.chunkCoordZ);

        ret.setInteger("x", (int)p.posX);
        ret.setInteger("z", (int)p.posZ);
        return ret;
    }

    public static ArrayList<String> chunkScanFromNBT(NBTTagCompound tag){
        ArrayList<String> rows = new ArrayList<String>();
        rows.add("Resources at (" + tag.getInteger("x") + ":" + tag.getInteger("z") + "):");

        NBTTagList list = tag.getTagList("ore_list", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound row = list.getCompoundTagAt(i);
            ItemStack itemStack = ItemStack.loadItemStackFromNBT(row.getCompoundTag("item"));
            if (itemStack != null){
                if (!skipPoory(itemStack.getDisplayName())) {
                    rows.add(" "+row.getInteger("count")+" x "+itemStack.getDisplayName());
                }
            }
        }

        if (tag.hasKey("fluid")) {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("fluid"));
            if (fluid != null){
                rows.add(" "+tag.getInteger("fluid_amount")+" x "+fluid.getLocalizedName());
            }
        }

        return rows;
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

    public static Map<ItemStack, Integer> scanChunk(Chunk c) {
        Map<ItemStack, Integer> scanResults = new HashMap<ItemStack, Integer>();
        int meta;
        boolean already_in_set;
        Block block;
        World w = c.worldObj;

        for (int y = c.getTopFilledSegment() + 16; y > 0; y--) {
            for (int x = (c.xPosition * 16); x < (c.xPosition * 16) + 16; x++) {
                for (int z = (c.zPosition * 16); z < (c.zPosition * 16) + 16; z++) {
                    if (!genericMineable(w, x, y, z)) continue;

                    block = w.getBlock(x, y, z);
                    meta = w.getBlockMetadata(x, y, z);
                    for (ItemStack drop : block.getDrops(w, x, y, z, meta, 0)) {
                        already_in_set = false;

                        for (Map.Entry<ItemStack, Integer> entry : scanResults.entrySet()) {
                            if (ItemStack.areItemStacksEqual(entry.getKey(), drop)){
                                entry.setValue(entry.getValue() + 1);
                                already_in_set = true;
                            }
                        }

                        if (!already_in_set) scanResults.put(drop, 1);
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
