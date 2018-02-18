package lime.chunk_miner.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

import java.util.HashMap;

public class ScanReportMessage implements IMessage {
    public ScanReportMessage(){}
    private NBTTagCompound payload;
    public ScanReportMessage(String payload){
        this.payload = payload;
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeBytes(payload);
    }

    @Override public void fromBytes(ByteBuf buf) {
        buf.readBytes(this.payload);
    }

    public static class Handler implements IMessageHandler<ScanReportMessage, IMessage> {

        @Override
        public IMessage onMessage(ScanReportMessage message, MessageContext ctx) {
            if (ctx.side.isClient() && message.payload != null)
            {
                EntityPlayer p = ChunkMiner.proxy.getPlayer(ctx);

                HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> map = Utils.mapFromNBT(message.payload);

                for(HashMap.Entry<Integer, HashMap<Integer, HashMap<String, Integer>>> x_entry : map.entrySet())
                {
                    int x = x_entry.getKey();

                    for(HashMap.Entry<Integer, HashMap<String, Integer>> z_entry : x_entry.getValue().entrySet())
                    {
                        int z = z_entry.getKey();

                        p.addChatMessage(new ChatComponentText("Scan of "+x+":"+z+":"));

                        for (HashMap.Entry<String, Integer> item_entry : z_entry.getValue().entrySet())
                        {
                            String name = Utils.nameFromString(item_entry.getKey());
                            if (!Utils.shouldBeSkipped(name)){
                                String row = " " + item_entry.getValue() + " x " + name;
                                p.addChatMessage(new ChatComponentText(row));
                            }
                        }
                    }
                }
            }
            return null;
        }

    }
}
