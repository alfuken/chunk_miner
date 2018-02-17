package lime.chunk_miner;

import cpw.mods.fml.common.Loader;
import gregtech.common.GT_UndergroundOil;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import lime.chunk_miner.blocks.ChunkMinerBlock;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

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

    public static boolean shouldBeSkipped(String name){
        for(String m : Config.ignored_materials){
            if (name.contains(m)) return true;
        }
        return false;
    }

    public static HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> scan(World w, int player_x, int player_z)
    {
        return scan(w, player_x, player_z, 0);
    }

    public static HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> scan(World w, int player_x, int player_z, int radius)
    {
        //      X                Z                Item    Count                           X                Z                Item    Count
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> theMap = new HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>();
        Chunk c = w.getChunkFromBlockCoords(player_x, player_z);

        for (int x = c.xPosition-radius; x <= c.xPosition+radius; x++) {
            for (int z = c.zPosition-radius; z <= c.zPosition+radius; z++) {
                try
                {
                    HashMap<Integer, HashMap<String, Integer>> x_value = theMap.get(x);

                    if (x_value == null) x_value = new HashMap<Integer, HashMap<String, Integer>>();

                    x_value.put(z, scanChunk(w.getChunkFromChunkCoords(x, z)));

                    theMap.put(x, x_value);
                }
                catch (NoSuchElementException e){e.printStackTrace(System.out);}
            }
        }

        return theMap;
    }

    public static HashMap<String, Integer> scanChunk(Chunk c)
    {
        HashMap<String, Integer> ret = new HashMap<String, Integer>();
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
                String key = fluidToString(fluidStack);
                ret.put(key, fluidStack.amount);
            }
        }

        return sortByValue(ret);

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
        DataOutputStream os = new DataOutputStream(baos);
        try {
            CompressedStreamTools.write(tag, os);
            os.close();
            baos.close();
        } catch (IOException io){
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
        NBTTagCompound tag = new NBTTagCompound();

        ByteArrayInputStream bais = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(s));
        DataInputStream is = new DataInputStream(bais);
        try {
            tag = CompressedStreamTools.read(is);
            is.close();
            bais.close();
        } catch (IOException io){
            io.printStackTrace(System.out);
        }

        return tag;
    }

    public static String nameFromString(String str){
        NBTTagCompound tag = tagFromString(str);
        if (isFluidTag(tag)){
            return FluidStack.loadFluidStackFromNBT(tag).getLocalizedName();
        } else {
            return ItemStack.loadItemStackFromNBT(tag).getDisplayName();
        }
    }

    public static String mapToString(HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> map){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            ObjectOutputStream os = new ObjectOutputStream(baos);
            os.writeObject(map);
            os.close();
        }
        catch (IOException io)
        {
            io.printStackTrace(System.out);
        }
        finally
        {
            try{baos.close();}catch(IOException io){io.printStackTrace(System.out);}
        }

        return DatatypeConverter.printBase64Binary(baos.toByteArray());
    }

    public static HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> mapFromString(String s){
        HashMap map = new HashMap();

        ByteArrayInputStream bais = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(s));
        try {
            ObjectInputStream is = new ObjectInputStream(bais);
            map = (HashMap) is.readObject();
            is.close();
            bais.close();
        }
        catch (IOException io)
        {
            io.printStackTrace(System.out);
        }
        catch(ClassNotFoundException c)
        {
            System.out.println("Class not found!");
            c.printStackTrace(System.out);
        }
        finally
        {
            try{bais.close();}catch(IOException io){io.printStackTrace(System.out);}
        }

        return map;
    }


}
