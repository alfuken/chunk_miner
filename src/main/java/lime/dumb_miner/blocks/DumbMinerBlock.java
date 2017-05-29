package lime.dumb_miner.blocks;

import lime.dumb_miner.DumbMiner;
import lime.dumb_miner.tiles.DumbMinerTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;

public class DumbMinerBlock extends BlockContainer {
    public DumbMinerBlock(){
        super(Material.iron);
        setCreativeTab(DumbMiner.ctab);
        setBlockName(DumbMiner.MODID + "_" + "dumb_miner_block");
        setBlockTextureName(DumbMiner.MODID + "_" + "dumb_miner_block");
        setHardness(1.0F);
        setResistance(2000.0F);
        setHarvestLevel("pickaxe", 0);
        setLightLevel(1.0F);
    }

    public IIcon[] icons = new IIcon[6];

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        for (int i = 0; i < 6; i ++) {
            this.icons[i] = reg.registerIcon(this.textureName + "_" + i);
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return this.icons[side];
    }

    public TileEntity createNewTileEntity(World w, int meta) {
        return new DumbMinerTile();
    }

    public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer p, int i1, float f1, float f2, float f3) {
        return ((DumbMinerTile)w.getTileEntity(x,y,z)).onUse();
    }

    public void onNeighborBlockChange(World w, int x, int y, int z, Block block) {
        if (w.isRemote) return;
        ((DumbMinerTile)w.getTileEntity(x,y,z)).setRedstone(w.isBlockIndirectlyGettingPowered(x, y, z));
    }

    public int quantityDropped(Random r) {
        return 0;
    }
}
