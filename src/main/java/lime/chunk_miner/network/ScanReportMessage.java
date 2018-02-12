package lime.chunk_miner.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.ChunkMinerHelpers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
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
                int n = 0;
                for(String row : ChunkMinerHelpers.chunkScanFromNBT(message.payload)){
                    p.addChatMessage(new ChatComponentText(row));
                    if (n++ > 5) break;
                }
            }
            return null;
        }

    }
}
