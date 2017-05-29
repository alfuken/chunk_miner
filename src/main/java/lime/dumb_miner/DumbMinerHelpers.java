package lime.dumb_miner;

import gregtech.api.util.GT_Utility;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

public class DumbMinerHelpers {

    public static boolean isGtChunk(int x, int z){
        return ((x / 16 - 1) % 3 == 0) && ((z / 16 - 1) % 3 == 0);
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

    public static String formattedChunkScanReport(World w, EntityPlayer p){
        Map<String, Integer> ores = scan(w, (int)p.posX, (int)p.posZ, true);

        StringBuilder s = new StringBuilder();
        s.append("Resources at (").append((int)p.posX).append(":").append((int)p.posZ).append(") – ");
        for (Map.Entry<String, Integer> entry : ores.entrySet()) {
            s.append(entry.getValue()).append(" ").append(entry.getKey()).append("; ");
        }

        FluidStack fluid = GT_Utility.getUndergroundOil(w, (int)p.posX, (int)p.posZ);
        if (fluid.amount > 0) s.append(fluid.amount/1000).append(" ").append(fluid.getLocalizedName());

        return s.toString();
    }

    public static Map<String, Integer> scan(World w, int wx, int wz) {
        return scan(w, wx, wz, false);
    }

    public static Map<String, Integer> scan(World w, int wx, int wz, boolean skipPoor) {
        long startTime = System.currentTimeMillis();

        Map<String, Integer> ret = new HashMap();

        Chunk c = w.getChunkFromBlockCoords(wx, wz);

        for (int y = c.getTopFilledSegment() + 16; y > 0; y--) {
            for (int x = c.xPosition * 16; x < c.xPosition * 16 + 16; x++) {
                for (int z = c.zPosition * 16; z < c.zPosition * 16 + 16; z++) {
                    Block block = w.getBlock(x, y, z);
                    int meta = w.getBlockMetadata(x, y, z);
                    if(isValuable(w, x, y, z)) {
                        Iterator drops = block.getDrops(w, x, y, z, meta, 0).iterator();

                        while(drops.hasNext()) {
                            ItemStack drop = (ItemStack)drops.next();
                            String key = drop.getItem().getItemStackDisplayName(drop);
                            if (skipPoor && (
                                key.contains(" Dust") ||
                                key.contains("Chipped ") ||
                                key.contains("Flawed ") ||
                                key.contains("Crushed"))
                            ){} else {
                                Integer count = ret.get(key);
                                if(count == null) count = 0;
                                count += drop.stackSize;
                                ret.put(key, count);
                            }
                        }
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        System.out.println("\n===> Scan took "+duration+" milliseconds");

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
