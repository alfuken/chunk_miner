package lime.chunk_miner;

import cpw.mods.fml.common.Loader;
import gregtech.GT_Mod;
import gregtech.common.GT_UndergroundOil;
import gregtech.common.blocks.GT_Block_Ores_Abstract;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import lime.chunk_miner.blocks.ChunkMinerBlock;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import org.apache.commons.lang3.ArrayUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.*;

public class Utils {
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
            return (potentiallyValuableBlock2(block));
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
        if (Loader.isModLoaded("gregtech") && block instanceof GT_Block_Ores_Abstract) return true;
        String n = block.getUnlocalizedName();
        return (block instanceof BlockOre
            || n.startsWith("tile.ore")
            || n.startsWith("blockOre")
            || n.equals("gt.blockores")
            || block instanceof BlockGlowstone
        );
    }

    public static boolean potentiallyValuableBlock(Block block){
        return (block.getUnlocalizedName().contains("ore") || block instanceof BlockGlowstone);
    }

    public static boolean trashBlock(Block block){
        return (block == null
             || block instanceof BlockAir
             || block.equals(Blocks.stone)
             || !mineable(block)
             || block instanceof ChunkMinerBlock
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
//        if (!trashBlock(block)){
//            System.out.println("-"+block+"/"+block.getUnlocalizedName()+" at "+x+":"+y+":"+z+ " = "+potentiallyValuableBlock2(block));
//        }
        if (Config.scan_mode.equals("optimistic")){
            return (potentiallyValuableBlock2(block));
        } else if (Config.scan_mode.equals("pessimistic")){
            return (potentiallyValuableBlock2(block));
        } else if (Config.scan_mode.equals("classic")) {
            return (isValuable(w, x, y, z));
        } else {
            return !trashBlock(block);
        }
    }

    public static boolean shouldBeSkipped(String name){
        for(String m : Config.ignored_materials){
            if (name.contains(m)) return true;
        }
        return false;
    }

    public static NBTTagList scan(World w, int player_x, int player_z)
    {
        return scan(w, player_x, player_z, 0);
    }

    public static NBTTagList scan(World w, int player_x, int player_z, int radius)
    {
        NBTTagList ret = new NBTTagList();
        Chunk c = w.getChunkFromBlockCoords(player_x, player_z);

        for (int x = c.xPosition-radius; x <= c.xPosition+radius; x++)
        {
            for (int z = c.zPosition-radius; z <= c.zPosition+radius; z++)
            {
                ret.appendTag(scanChunk(w.getChunkFromChunkCoords(x, z)));
            }
        }

        return ret;
    }

    public static NBTTagCompound scanChunk(Chunk c)
    {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
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
                        String key = itemToString(drop);
                        Integer count = map.get(key);
                        if (count == null) count = 0;
                        map.put(key, ++count);
                    }
                }
            }
        }

        if (Loader.isModLoaded("gregtech")){
            int[] blacklist = GT_Mod.gregtechproxy.mUndergroundOil.BlackList;
            if (w.provider.dimensionId != -1 && w.provider.dimensionId != 1){
                FluidStack fluidStack = GT_UndergroundOil.undergroundOil(c, -1.0F);
                if (fluidStack != null){
                    map.put(fluidToString(fluidStack), fluidStack.amount);
                }
            }
        }

        map = sortByValue(map);

        NBTTagCompound ret = new NBTTagCompound();
        ret.setInteger("x", c.xPosition);
        ret.setInteger("z", c.zPosition);

        for(HashMap.Entry<String, Integer> e : map.entrySet())
        {
            ret.setInteger(e.getKey(), e.getValue());
        }

        return ret;
    }

    public static <K, V extends Comparable<? super V>> HashMap<K, V> sortByValue(HashMap<K, V> map) {
        List<HashMap.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        });

        HashMap<K, V> result = new LinkedHashMap<K, V>();
        for (HashMap.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }

    public static boolean isFluidTag(NBTTagCompound tag){
        return tag.hasKey("FluidName");
    }

    public static String itemToString(ItemStack item){
        return tagToString(item.writeToNBT(new NBTTagCompound()));
    }

    public static String fluidToString(FluidStack fluid){
        return tagToString(fluid.writeToNBT(new NBTTagCompound()));
    }

    public static String tagToString(NBTTagCompound tag){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream        os = new DataOutputStream(baos);
        try
        {
            CompressedStreamTools.write(tag, os);
            os.close();
            baos.close();
        }
        catch (IOException io)
        {
            io.printStackTrace(System.out);
        }

        return DatatypeConverter.printBase64Binary(baos.toByteArray());
    }

    public static ItemStack itemFromString(String s){
        return ItemStack.loadItemStackFromNBT(tagFromString(s));
    }

    public static FluidStack fluidFromString(String s){
        return FluidStack.loadFluidStackFromNBT(tagFromString(s));
    }

    public static NBTTagCompound tagFromString(String s){
        NBTTagCompound        tag = new NBTTagCompound();
        ByteArrayInputStream bais = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(s));
        DataInputStream        is = new DataInputStream(bais);

        try
        {
            tag = CompressedStreamTools.read(is);
            is.close();
            bais.close();
        }
        catch (IOException io)
        {
            io.printStackTrace(System.out);
        }

        return tag;
    }

    public static String nameFromString(String s){
        NBTTagCompound tag = tagFromString(s);

        if (isFluidTag(tag))
        {
            return FluidStack.loadFluidStackFromNBT(tag).getLocalizedName();
        }
        else
        {
            return ItemStack.loadItemStackFromNBT(tag).getDisplayName();
        }
    }
}
