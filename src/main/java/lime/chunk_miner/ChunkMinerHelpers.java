package lime.chunk_miner;

import cpw.mods.fml.common.Loader;
import gregtech.common.GT_UndergroundOil;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import lime.chunk_miner.blocks.ChunkMinerBlock;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.*;

public class ChunkMinerHelpers {
    public static boolean isGtChunk(int x, int z){
        return ((x / 16 - 1) % 3 == 0) && ((z / 16 - 1) % 3 == 0);
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

    public static boolean mineableNonStone(Block block){
        return(mineable(block) && !(block instanceof BlockStone));
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

    private static boolean shouldBeSkipped(String name){
        if (Config.skip_poor_ores) {
            for(String m : Config.ignored_materials){
                if (name.contains(m)) return true;
            }
        }
        return false;
    }

    public static List<NBTTagCompound> areaScanResultsAsNBTList(World w, int wx, int wz, int radius) {
        List<NBTTagCompound> ret = new ArrayList<NBTTagCompound>();
        Chunk c = w.getChunkFromBlockCoords(wx, wz);

        for (int x = c.xPosition-radius; x <= c.xPosition+radius; x++) {
            for (int z = c.zPosition-radius; z <= c.zPosition+radius; z++) {
                try {
                    Chunk chunk = w.getChunkFromChunkCoords(x, z);
                    ret.add(chunkScanResultsAsTag(chunk));
                } catch (NoSuchElementException e){}
            }
        }

        return ret;
    }

    public static NBTTagCompound chunkScanResultsAsTag(Chunk c){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("x", c.xPosition);
        tag.setInteger("z", c.zPosition);
        NBTTagList list = new NBTTagList();
        for(NBTTagCompound entry : chunkScanResultsAsNBTList(c)){
            list.appendTag(entry);
        }
        tag.setTag("list", list);
        return tag;
    }

    public static List<NBTTagCompound> chunkScanResultsAsNBTList(Chunk c){
        List<NBTTagCompound> ret = new ArrayList<NBTTagCompound>();
        NBTTagCompound tag;

        for(Map.Entry<String, Integer> entry : chunkScanResultsAsMap(c).entrySet()){
            tag = new NBTTagCompound();
            tag.setString("item", entry.getKey());
            tag.setInteger("n", entry.getValue());
            ret.add(tag);
        }

        return ret;
    }

    public static Map<String, Integer> chunkScanResultsAsMap(Chunk c) {
        Map<String, Integer> ret = new HashMap<String, Integer>();
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
                        if (shouldBeSkipped(drop.getItem().getItemStackDisplayName(drop))) continue;

                        String key = ScanDB.itemToString(drop);
                        Integer count = ret.get(key);
                        if (count == null) count = 0;
                        ret.put(key, ++count);
                    }
                }
            }
        }

        if (Loader.isModLoaded("gregtech")){
            FluidStack fluidStack = GT_UndergroundOil.undergroundOil(c, -1.0F);
            if (fluidStack != null){
                NBTTagCompound fluid_tag = new NBTTagCompound();
                fluidStack.writeToNBT(fluid_tag);
                String key = ScanDB.tagToString(fluid_tag);
                ret.put(key, fluidStack.amount);
            }
        }

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
