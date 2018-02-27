package lime.chunk_miner.tiles;

import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.Utils;
import lime.chunk_miner.Config;
import lime.chunk_miner.blocks.ChunkMinerBlock;
import lime.chunk_miner.network.SaveScanReportMessage;
import net.minecraft.block.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.Iterator;
import java.util.List;

import static ic2.core.util.StackUtil.distribute;

public class ChunkMinerTile extends TileEntity {
    private int update_cooldown = 20;
    private int work_progress = 0;
    private int seconds_to_mine = Config.seconds_to_mine;
    private int cooldown = seconds_to_mine;
    private int currX = -1;
    private int currY = -1;
    private int currZ = -1;
    private boolean redstone = false;
    private ForgeChunkManager.Ticket chunkTicket;
    public int mode = 0;
    private EntityPlayer owner;
    private String owner_name;
    private Chunk currChunk;

    private void setChunk(){
        this.currChunk = this.getWorldObj().getChunkFromBlockCoords(this.xCoord, this.zCoord);
    }
    public void setRedstone(boolean value){
        this.redstone = value;
    }
    public void setOwner(EntityPlayer owner){
        if (owner == null) return;
        this.owner = owner;
        this.owner_name = owner.getCommandSenderName();
    }
    public EntityPlayer getOwner(){
        if (owner == null && owner_name != null) setOwner(this.getWorldObj().getPlayerEntityByName(owner_name));
        return this.owner;
    }

    public void updateEntity(){
        if (this.worldObj.isRemote) return;

        if (update_cooldown > 0) {
            update_cooldown--;
            return;
        } else {
            update_cooldown = 20;
        }

        setUp();
        update();
    }

    private void setUp(){
        if (owner == null && owner_name != null) setOwner(this.getWorldObj().getPlayerEntityByName(owner_name));
        if (currChunk == null) setChunk();
        if (currY == -1) currY = mineFromY();
    }

    private void update(){
        if (Config.require_redstone) {
            ChunkMinerBlock.updateRSStatus(this.worldObj, this.xCoord,this.yCoord, this.zCoord);
            if (!redstone) return;
        }

        if (cooldown > 0){
            cooldown--;
        } else {
            cooldown = seconds_to_mine;
            if (this.work()) this.markDirty();
        }
    }

    public void nextMode(){
        if (mode == 0){
            mode = 1;
        } else if (mode == 1) {
            mode = 2;
        } else if (mode == 2) {
            mode = 0;
        } else {
            mode = 0;
        }
        currY = mineFromY();
        updateCooldownSize();
    }

    private void updateCooldownSize(){
        seconds_to_mine = (mode == 0 || mode == 1) ? Config.seconds_to_mine : Config.seconds_to_mine / 2;
        if (seconds_to_mine < 1) seconds_to_mine = 1;
    }

    public void statusReport(EntityPlayer player){
        String rs_status = "";
        if (Config.require_redstone) {
            rs_status += "[Redstone ";
            rs_status += redstone ? "ON" : "OFF";
            rs_status += "] ";
        }
        player.addChatMessage(new ChatComponentText(rs_status+"Mining block at "+currX+":"+currY+":"+currZ+", from "+mineFromY()+" to bedrock. Cooldown: "+ cooldown +"s of "+ seconds_to_mine +""));
    }

    public void report(String text) {
        getOwner().addChatMessage(new ChatComponentText(text));
    }

    public boolean onUse(){
        if (this.worldObj.isRemote) return false;
        if (work_progress >= Config.work_to_mine){
            work_progress = 0;
            if (this.work()) this.markDirty();
        } else {
            work_progress++;
        }
        return false;
    }

    private int mineFromY(){
        return this.worldObj.getChunkFromBlockCoords(this.xCoord, this.zCoord).getTopFilledSegment() + 16;
//        return this.yCoord - 1;
    }

    private boolean work() {
        if (this.worldObj.isRemote) return false;

        while (currY > 0) {
            for (int x = (currChunk.xPosition * 16); x < (currChunk.xPosition * 16) + 16; x++) {
                for (int z = (currChunk.zPosition * 16); z < (currChunk.zPosition * 16) + 16; z++) {
                    currX = x;
                    currZ = z;
                    if (mine()) return true;
                }
            }
            currY--;
        }

        if (this.currY == 0) finish();

        return false;
    }

    private boolean mine(){
        Block block = this.worldObj.getBlock(this.currX, this.currY, this.currZ);
        if (mineable(block)) {
            int meta = this.worldObj.getBlockMetadata(this.currX, this.currY, this.currZ);
            List<ItemStack> drops = block.getDrops(this.worldObj, this.currX, this.currY, this.currZ, meta, 0);
            distributeDrop(this, drops);
            this.worldObj.setBlockToAir(this.currX, this.currY, this.currZ);
            return true;
        } else if (shouldRemoveFluidblock(block)) {
            this.worldObj.setBlockToAir(this.currX, this.currY, this.currZ);
        }
        return false;
    }

    private boolean shouldRemoveFluidblock(Block block){
        // TODO: add config to disable water and/or lava removal
        if (mode == 0) return false;
        return(block instanceof IFluidBlock || block instanceof BlockLiquid);
    }

    private void finish(){
        if (getOwner() != null) {
            getOwner().addChatMessage(new ChatComponentText("> Miner at " + this.xCoord + ":" + this.zCoord + " have finished mining."));
            NBTTagList data = Utils.scan(this.getWorldObj(), this.xCoord, this.zCoord);
            ChunkMiner.network.sendTo(new SaveScanReportMessage(data), (EntityPlayerMP) getOwner());
        }
        if (Config.selfdestruct) {
            getWorldObj().setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
        } else {
            setRedstone(false);
        }
    }

    private boolean mineable(Block block) {
        if (this.mode == 0) {
            return Utils.genericMineable(getWorldObj(), currX, currY, currZ);
        } else if (this.mode == 1) {
            return Utils.mineableNonStone(block);
        } else if (this.mode == 2) {
            return Utils.mineable(block);
        } else {
            return false;
        }
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
            entityItem.motionY = 0.25;
            entityItem.motionZ = 0;
            world.spawnEntityInWorld(entityItem);
        }
    }


    @Override
    public void validate() {
        super.validate();
        if (Config.load_chunks && (!this.worldObj.isRemote) && (this.chunkTicket == null)) {
            ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(ChunkMiner.INSTANCE, this.worldObj, ForgeChunkManager.Type.NORMAL);
            if (ticket != null) forceChunkLoading(ticket);
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

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("currX", this.currX);
        compound.setInteger("currY", this.currY);
        compound.setInteger("currZ", this.currZ);
        compound.setInteger("mode", this.mode);
        compound.setString("owner_name", this.owner_name+"");
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.currX = compound.getInteger("currX");
        this.currY = compound.getInteger("currY");
        this.currZ = compound.getInteger("currZ");
        this.mode = compound.getInteger("mode");
        this.owner_name = compound.getString("owner_name");
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
