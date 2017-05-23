package lime.gtminer.items;

import gregtech.api.GregTech_API;
import gregtech.api.enums.Materials;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Utility;
import gregtech.common.blocks.GT_Block_Ores_Abstract;
import gregtech.common.blocks.GT_TileEntity_Ores;
import lime.gtminer.GtMiner;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;

import java.util.Hashtable;
import java.util.Set;

public class GtOreFinder extends Item {
    public GtOreFinder() {
        setCreativeTab(GtMiner.ctab);
        setUnlocalizedName(GtMiner.MODID + "_" + "gtOreFinder");
        setTextureName(GtMiner.MODID + "_" + "gtOreFinder");
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World w, EntityPlayer p) {
        if (!w.isRemote){return itemStack;}
        int x = (int)p.posX;
        int z = (int)p.posZ;

        if (isGtChunk(x, z)){ p.addChatMessage(new ChatComponentText("GT Chunk!")); }

        Hashtable<String, int[]> oresList = scanChunk(w, p);

        StringBuilder s = new StringBuilder();
        if(oresList.isEmpty()){
            s.append("No Ores found.");
        } else {
            s.append("Ores in this chunk:\n");
            Set<String> keys = oresList.keySet();
            for(String key: keys){
                int[] heights = oresList.get(key);
                String row = " ("+heights[0]+"): "+heights[1];
                if (heights[1] != heights[2]){row += "-"+heights[2];}
                s.append(key).append(row).append("\n");
            }
        }

        GtMiner.chunk_cache.put(x+":"+z, s.toString());
        GtMiner.updateCurrentChunkCacheData(x+":"+z);
        p.addChatMessage(new ChatComponentText(s.toString()));

        return itemStack;
    }

    private boolean isGtChunk(int x, int z){
        return ((x / 16 - 1) % 3 == 0) && ((z / 16 - 1) % 3 == 0);
    }

    private Hashtable scanChunk(World w, EntityPlayer p){
        long startTime = System.currentTimeMillis();

        Hashtable<String, int[]> oresList = new Hashtable<String, int[]>();

        Chunk c = w.getChunkFromBlockCoords((int)p.posX, (int)p.posZ);

        for (int y = 0; y <= c.getTopFilledSegment() + 16; y++) {
            for (int x = c.xPosition*16; x < c.xPosition*16+16; x++) {
                for (int z = c.zPosition*16; z < c.zPosition*16+16; z++) {
                    Block b = w.getBlock(x, y, z);

                    // skip the useless check for air and stone
                    if (b != Blocks.air && b != Blocks.stone){
                        String key = "";

                        if(b instanceof GT_Block_Ores_Abstract) {
                            TileEntity tTileEntity = w.getTileEntity(x, y, z);
                            if(tTileEntity instanceof GT_TileEntity_Ores && ((GT_TileEntity_Ores)tTileEntity).mMetaData < 16000) {
                                Materials tMaterial = GregTech_API.sGeneratedMaterials[((GT_TileEntity_Ores)tTileEntity).mMetaData % 1000];
                                if(tMaterial != null && tMaterial != Materials._NULL && tMaterial != Materials.Empty) {
                                    key = tMaterial.mDefaultLocalName;
                                }
                            }
                        } else {
                            int tMetaID = w.getBlockMetadata(x, y, z);
                            ItemData tAssotiation = GT_OreDictUnificator.getAssociation(new ItemStack(b, 1, tMetaID));
                            if(tAssotiation != null && tAssotiation.mPrefix.toString().contains("ore")) {
                                key = tAssotiation.mMaterial.mMaterial.mDefaultLocalName;
                            }
                        }

                        if (key != "" && key != "Empty"){
                            int[] ore_data = oresList.get(key);
                            if (ore_data == null) {
                                oresList.put(key, new int[]{1, y, y}); // count, y-min, y-max
                            } else {
                                if (y > ore_data[2]){ // new y > stored y-max
                                    oresList.put(key, new int[]{ore_data[0]+1, ore_data[1], y});
                                } else {
                                    oresList.put(key, new int[]{ore_data[0]+1, ore_data[1], ore_data[2]});
                                }
                            }
                        }
                    }
                }
            }
        }

        FluidStack tFluid = GT_Utility.getUndergroundOil(w, (int)p.posX, (int)p.posZ);
        oresList.put(tFluid.getLocalizedName(), new int[]{tFluid.amount/5000, 0, 0});

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        System.out.println("\n===> Scan took "+duration+" milliseconds");

        return (oresList);
    }
}
