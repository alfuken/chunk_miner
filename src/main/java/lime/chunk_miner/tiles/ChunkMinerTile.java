package lime.chunk_miner.tiles;

import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.ChunkMinerHelpers;
import lime.chunk_miner.Config;
import lime.chunk_miner.blocks.ChunkMinerBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.Iterator;
import java.util.List;

import static ic2.core.util.StackUtil.distribute;

public class ChunkMinerTile extends TileEntity {
    private int boot_up_cooldown = 10;
    private int work_progress = 0;
    private int ticker = 0;
    private int currX = -1;
    private int currY = -1;
    private int currZ = -1;
    private boolean redstone = false;
    private ForgeChunkManager.Ticket chunkTicket;

    public void setRedstone(boolean value){
        this.redstone = value;
    }

    public void updateEntity(){
        if (this.worldObj.isRemote) return;

        if (boot_up_cooldown == 0) {
            ChunkMinerBlock.updateRSStatus(this.worldObj, this.xCoord,this.yCoord, this.zCoord);
        } else {
            boot_up_cooldown--;
        }

        if (Config.require_redstone && !redstone) return;

        if (ticker >= Config.seconds_to_mine * 20){
            ticker = 0;
            if (this.work()) this.markDirty();
        } else {
            ticker++;
        }
    }

    public void statusReport(EntityPlayer player){
        player.addChatMessage(new ChatComponentText(currX+":"+currY+":"+currZ+" r:"+redstone+" m:"+mineFromY()+"-"+mineToY()));
    }

    public boolean onUse(){
        if (this.worldObj.isRemote) return true;
        if (work_progress >= Config.work_to_mine){
            work_progress = 0;
            if (this.work()) this.markDirty();
        } else {
            work_progress++;
        }
        return true;
    }

    private int mineFromY(){
        return Config.levels_to_mine == 0 ? this.worldObj.getChunkFromBlockCoords(this.xCoord, this.zCoord).getTopFilledSegment() + 16 : this.yCoord - 1;
    }

    private int mineToY(){
        return Config.levels_to_mine == 0 ? 0 : this.yCoord - Config.levels_to_mine;
    }

    private boolean work() {
        if (this.worldObj.isRemote) return false;

        checkYLimit(currY);

        Chunk c = this.worldObj.getChunkFromBlockCoords(this.xCoord, this.zCoord);

        if (currY == -1) currY = mineFromY();

        for (int y = currY; y > mineToY(); y--) {
            checkYLimit(y);
            for (int x = c.xPosition * 16; x < c.xPosition * 16 + 16; x++) {
                for (int z = c.zPosition * 16; z < c.zPosition * 16 + 16; z++) {
                    currX = x;
                    currY = y;
                    currZ = z;
                    if (shouldMine()) return doMine();
                }
            }
        }

        return false;
    }

    private void checkYLimit(int y){
        if (y == mineToY()) {
            System.out.println("----- DESTROY -----");
            if (Config.selfdestruct) {
                getWorldObj().setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
            } else {
                setRedstone(false);
            }
        }
    }

    private boolean doMine() {
        Block block = this.worldObj.getBlock(this.currX, this.currY, this.currZ);
        distributeDrop(this, block.getDrops(this.worldObj, this.currX, this.currY, this.currZ, this.worldObj.getBlockMetadata(this.currX, this.currY, this.currZ), 0));
        this.worldObj.setBlockToAir(this.currX, this.currY, this.currZ);
        return true;
    }

    private boolean shouldMine() {
        return ChunkMinerHelpers.shouldMine(this.worldObj, this.currX, this.currY, this.currZ);
    }

    private static void distributeDrop(TileEntity source, List<ItemStack> itemStacks) {
        Iterator it = itemStacks.iterator();

        ItemStack itemStack;
        while(it.hasNext()) {
            itemStack = (ItemStack)it.next();
            int amount = distribute(source, itemStack, false);
            if(amount == itemStack.stackSize) {
                it.remove();
            } else {
                itemStack.stackSize -= amount;
            }
        }

        it = itemStacks.iterator();

        while(it.hasNext()) {
            itemStack = (ItemStack)it.next();
            dropAsEntity(source.getWorldObj(), source.xCoord, source.yCoord+1, source.zCoord, itemStack);
        }

        itemStacks.clear();
    }

    private static void dropAsEntity(World world, int x, int y, int z, ItemStack itemStack) {
        if(itemStack != null) {
            double f = 0.7D;
            double dx = (double)world.rand.nextFloat() * f + (1.0D - f) * 0.5D;
            double dy = (double)world.rand.nextFloat() * f + (1.0D - f) * 0.5D;
            double dz = (double)world.rand.nextFloat() * f + (1.0D - f) * 0.5D;
            EntityItem entityItem = new EntityItem(world, (double)x + dx, (double)y + dy, (double)z + dz, itemStack.copy());
            entityItem.delayBeforeCanPickup = 10;
            entityItem.motionX = 0;
            entityItem.motionY = 0.15;
            entityItem.motionZ = 0;
            world.spawnEntityInWorld(entityItem);
        }
    }


    @Override
    public void validate() {
        super.validate();
        if (Config.load_chunks && (!this.worldObj.isRemote) && (this.chunkTicket == null)) {
            ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(ChunkMiner.INSTANCE, this.worldObj, ForgeChunkManager.Type.NORMAL);
            if (ticket != null) {
                forceChunkLoading(ticket);
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (Config.load_chunks) stopChunkLoading();
    }

    public void forceChunkLoading(ForgeChunkManager.Ticket ticket) {
        if (!Config.load_chunks) return;
        stopChunkLoading();
        this.chunkTicket = ticket;

        if (ticket != null) {
            ticket.getModData().setInteger("x", this.xCoord);
            ticket.getModData().setInteger("y", this.yCoord);
            ticket.getModData().setInteger("z", this.zCoord);
            ForgeChunkManager.forceChunk(this.chunkTicket, new ChunkCoordIntPair(this.xCoord / 16, this.zCoord / 16));
        }
    }

    private void stopChunkLoading() {
        if (Config.load_chunks && this.chunkTicket != null) {
            ForgeChunkManager.releaseTicket(this.chunkTicket);
            this.chunkTicket = null;
        }
    }

//    @Override
//    public Packet getDescriptionPacket()
//    {
//        NBTTagCompound tag = new NBTTagCompound();
//        writeToNBT(tag);
//        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
//    }
//
//    @Override
//    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
//    {
//        readFromNBT(packet.func_148857_g());
//    }

}
