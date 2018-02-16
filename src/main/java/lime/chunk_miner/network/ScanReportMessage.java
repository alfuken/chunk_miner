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

public class ScanReportMessage implements IMessage {
    public ScanReportMessage(){}
    private NBTTagCompound payload;
    public ScanReportMessage(NBTTagCompound payload){
        this.payload = payload;
    }

    @Override public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, payload);
    }

    @Override public void fromBytes(ByteBuf buf) {
        this.payload = ByteBufUtils.readTag(buf);
    }

    public static class Handler implements IMessageHandler<ScanReportMessage, IMessage> {

        @Override
        public IMessage onMessage(ScanReportMessage message, MessageContext ctx) {
            if (ctx.side.isClient() && message.payload != null){
                EntityPlayer p = ChunkMiner.proxy.getPlayer(ctx);
                int x = message.payload.getInteger("x") * 16 + 8;
                int z = message.payload.getInteger("z") * 16 + 8;
                p.addChatMessage(new ChatComponentText("Scan of "+x+":"+z+":"));
                NBTTagList list = message.payload.getTagList("list", message.payload.getId());
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound nbt = list.getCompoundTagAt(i);
                    String row = " " + nbt.getInteger("n") + " x " + ScanDB.itemFromString(nbt.getString("item")).getDisplayName();
                    p.addChatMessage(new ChatComponentText(row));
                }
            }
            return null;
        }

    }
}
