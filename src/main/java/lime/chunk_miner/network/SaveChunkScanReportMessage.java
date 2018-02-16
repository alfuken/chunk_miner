package lime.chunk_miner.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.ScanDB;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;


public class SaveChunkScanReportMessage implements IMessage {
    public SaveChunkScanReportMessage(){}
    private NBTTagCompound payload;
    public SaveChunkScanReportMessage(NBTTagCompound payload){
        this.payload = payload;
    }

    @Override public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, payload);
    }
    @Override public void fromBytes(ByteBuf buf) {
        this.payload = ByteBufUtils.readTag(buf);
    }

    public static class Handler implements IMessageHandler<SaveChunkScanReportMessage, IMessage> {

        class SaveChunkScanReportThread extends Thread {
            private EntityPlayer p;
            private NBTTagCompound nbt;
            public SaveChunkScanReportThread(EntityPlayer p, NBTTagCompound nbt) {
                super();
                this.p = p;
                this.nbt = nbt;
            }

            private void saveChunk(NBTTagCompound tag){
                int x = tag.getInteger("x");
                int z = tag.getInteger("z");
                ScanDB.p(p).delete(x, z);
                NBTTagList list = tag.getTagList("list", tag.getId());
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound list_item = list.getCompoundTagAt(i);
                    ScanDB.p(this.p).insert(list_item.getString("item"), x, z, list_item.getInteger("n"));
                }
            }

            public void run() {
                ScanDB.p(p).lock();

                if (nbt.hasKey("big_batch"))
                {
                    NBTTagList list = nbt.getTagList("tag_list", nbt.getId());
                    for (int i = 0; i < list.tagCount(); i++)
                    {
                        saveChunk(list.getCompoundTagAt(i));
                    }
                }
                else
                {
                    saveChunk(nbt);
                }

                double time_taken = (System.currentTimeMillis() - ScanDB.p(p).locked_at)/1000.0;
                ScanDB.p(p).unlock();
                p.addChatMessage(new ChatComponentText(String.format("Scan complete in %f seconds.", time_taken)));
            }
        }

        @Override
        public IMessage onMessage(SaveChunkScanReportMessage message, MessageContext ctx) {
            if (ctx.side.isClient() && message.payload != null){
                EntityPlayer p = ChunkMiner.proxy.getPlayer(ctx);
//                NBTTagCompound nbt = message.payload;
//                int x = nbt.getInteger("x");
//                int z = nbt.getInteger("z");
//                ScanDB.p(p).delete(x, z);
//                NBTTagList list = nbt.getTagList("list", nbt.getId());
//                for (int i = 0; i < list.tagCount(); i++) {
//                    NBTTagCompound list_item = list.getCompoundTagAt(i);
//                    ScanDB.p(p).insert(list_item.getString("item"), x, z, list_item.getInteger("n"));
//                }
                if (ScanDB.p(p).locked_at > 0) {
                    p.addChatMessage(new ChatComponentText("Scan already in progress. Try again later."));
                } else {
                    new SaveChunkScanReportThread(p, message.payload).start();
                }
            }
            return null;
        }

    }

}
