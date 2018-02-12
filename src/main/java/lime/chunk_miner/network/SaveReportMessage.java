package lime.chunk_miner.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lime.chunk_miner.ChunkMiner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import static lime.chunk_miner.ChunkMinerHelpers.saveScanData;

public class SaveReportMessage implements IMessage {
    public SaveReportMessage(){}
    private NBTTagCompound payload;
    public SaveReportMessage(NBTTagCompound payload){
        this.payload = payload;
    }

    @Override public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, payload);
    }
    @Override public void fromBytes(ByteBuf buf) {
        this.payload = ByteBufUtils.readTag(buf);
    }

    public static class Handler implements IMessageHandler<SaveReportMessage, IMessage> {

        @Override
        public IMessage onMessage(SaveReportMessage message, MessageContext ctx) {
            if (ctx.side.isClient() && message.payload != null){
                EntityPlayer p = ChunkMiner.proxy.getPlayer(ctx);
                saveScanData(p, message.payload);
            }
            return null;
        }

    }

}
