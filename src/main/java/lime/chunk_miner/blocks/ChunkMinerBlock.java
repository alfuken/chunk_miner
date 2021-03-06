package lime.chunk_miner.blocks;

import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.tiles.ChunkMinerTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Direction;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

public class ChunkMinerBlock extends BlockContainer {
    ChunkMinerBlock(){
        super(Material.iron);
        setCreativeTab(ChunkMiner.ctab);
        setBlockName(ChunkMiner.MODID + ".chunk_miner_block");
        setBlockTextureName(ChunkMiner.MODID + ".chunk_miner_block");
        setHardness(1.0F);
        setResistance(2000.0F);
        setHarvestLevel("pickaxe", 0);
        setLightLevel(1.0F);
    }

    private IIcon[] icons = new IIcon[6];

    // 0-1-2-3-4-5 = D-U-F-L-B-R
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        for (int i = 0; i < 6; i ++) {
            this.icons[i] = reg.registerIcon(this.textureName + "_" + i);
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side <= 1) return this.icons[side];
        int index = (Direction.facingToDirection[side]-2) & 3;
        if (meta == 1) { index = Direction.rotateLeft[index]; }
        else if (meta == 2) { index = Direction.rotateOpposite[index]; }
        else if (meta == 3) { index = Direction.rotateRight[index]; }
        return this.icons[index+2];
    }

    @Override
    public int getDamageValue(World world, int x, int y, int z) {
        return 3;
    }

    public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase p, ItemStack itemStack) {
        int meta = MathHelper.floor_double((double)(p.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        super.onBlockPlacedBy(w, x, y, z, p, itemStack);
        w.setBlockMetadataWithNotify(x, y, z, meta, 2); // 0=S 1=W 2=N 3=E
        ChunkMinerTile t = (ChunkMinerTile)w.getTileEntity(x,y,z);
        t.setOwner((EntityPlayer)p);

    }

    public TileEntity createNewTileEntity(World w, int meta) {
        return new ChunkMinerTile();
    }

    public void onBlockClicked(World w, int x, int y, int z, EntityPlayer p) {
        if (w.isRemote) return;
        if (p.getCurrentEquippedItem() == null) {
            ChunkMinerTile m = ((ChunkMinerTile)w.getTileEntity(x,y,z));
            m.nextMode();
            String text = "everything! (Mines twice as fast)";
            if (m.mode == 0) text = "only ore.";
            if (m.mode == 1) text = "everything except stone.";
            p.addChatMessage(new ChatComponentText("Miner set to mine " + text));
        }
    }

    public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer p, int side, float xOffset, float yOffset, float zOffset) {
        if (w.isRemote) return false;
        ChunkMinerTile m = ((ChunkMinerTile)w.getTileEntity(x,y,z));
        if (p.getCurrentEquippedItem() == null) {
            if (p.isSneaking()) {
                m.statusReport(p);
            } else {
                m.onUse();
                return true;
            }
        } else if (p.getCurrentEquippedItem().getItem() == Items.stick) {
            m.nextMode();
            String text = "everything! (Mines twice as fast)";
            if (m.mode == 0) text = "only ore.";
            if (m.mode == 1) text = "everything except stone.";
            p.addChatMessage(new ChatComponentText("Miner set to mine " + text));
        }
        return false;
    }

    public void onNeighborBlockChange(World w, int x, int y, int z, Block block) {
        if (w.isRemote) return;
        updateRSStatus(w, x,y,z);
    }

    public static void updateRSStatus(World w, int x, int y, int z){
        ((ChunkMinerTile)w.getTileEntity(x,y,z)).setRedstone(w.isBlockIndirectlyGettingPowered(x, y, z));
    }

    public int quantityDropped(Random r) {
        return 0;
    }
}
