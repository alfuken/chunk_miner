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

//        class SaveChunkScanReportThread extends Thread {
//            private EntityPlayer p;
//            private NBTTagCompound nbt;
//            public SaveChunkScanReportThread(EntityPlayer p, NBTTagCompound nbt) {
//                super();
//                this.p = p;
//                this.nbt = nbt;
//            }
//            public void run() {
//                int x = nbt.getInteger("x");
//                int z = nbt.getInteger("z");
//                ScanDB.p(p).delete(x, z);
//                NBTTagList list = nbt.getTagList("list", nbt.getId());
//                for (int i = 0; i < list.tagCount(); i++) {
//                    NBTTagCompound list_item = list.getCompoundTagAt(i);
//                    ScanDB.p(this.p).insert(list_item.getString("item"), x, z, list_item.getInteger("n"));
//                }
//            }
//        }

        @Override
        public IMessage onMessage(SaveChunkScanReportMessage message, MessageContext ctx) {
            if (ctx.side.isClient() && message.payload != null){
                NBTTagCompound nbt = message.payload;
                EntityPlayer p = ChunkMiner.proxy.getPlayer(ctx);
                int x = nbt.getInteger("x");
                int z = nbt.getInteger("z");
                ScanDB.p(p).delete(x, z);
                NBTTagList list = nbt.getTagList("list", nbt.getId());
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound list_item = list.getCompoundTagAt(i);
                    ScanDB.p(p).insert(list_item.getString("item"), x, z, list_item.getInteger("n"));
                }
//                new SaveChunkScanReportThread(p, message.payload).start();
            }
            return null;
        }

    }

}
