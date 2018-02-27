package lime.chunk_miner.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;

import java.util.HashMap;

public class PrintScanReportMessage implements IMessage {
    public PrintScanReportMessage(){}
    private NBTTagList payload;
    public PrintScanReportMessage(NBTTagList payload){
        this.payload = payload;
    }

    @Override public void toBytes(ByteBuf buf)
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("list", this.payload);
        ByteBufUtils.writeTag(buf, tag);
    }

    @Override public void fromBytes(ByteBuf buf)
    {
        this.payload = ByteBufUtils.readTag(buf).getTagList("list", (new NBTTagCompound()).getId());
    }

    public static class Handler implements IMessageHandler<PrintScanReportMessage, IMessage> {

        @Override
        public IMessage onMessage(PrintScanReportMessage message, MessageContext ctx)
        {
            if (ctx.side.isClient() && message.payload != null)
            {
                EntityPlayer p = ChunkMiner.proxy.getPlayer(ctx);

                for (int i = 0; i < message.payload.tagCount(); i++)
                {
                    HashMap<String, Integer> map = new HashMap<String, Integer>();
                    NBTTagCompound tag = message.payload.getCompoundTagAt(i);
                    int x = tag.getInteger("x");
                    int z = tag.getInteger("z");

                    for (Object _item : tag.func_150296_c())
                    {
                        String item = (String)_item;
                        if (!item.equals("x") && !item.equals("z"))
                        {
                            String name = Utils.nameFromString(item);
                            if (Utils.shouldBeSkipped(name))
                            {
                                NBTTagCompound itemtag = Utils.tagFromString(item);
                                if (!Utils.isFluidTag(itemtag)){
                                    ItemStack the_item = Utils.itemFromString(item);
                                    if (the_item.getItem() == Items.glowstone_dust){
                                        map.put(name, tag.getInteger(item));
                                    }
                                }
                            }
                            else
                            {
                                map.put(name, tag.getInteger(item));
                            }

                        }
                    }

                    p.addChatMessage(new ChatComponentText("Scan of "+x+":"+z+":"));
                    for (HashMap.Entry<String, Integer> e : Utils.sortByValue(map).entrySet()){
                        String row = " " + e.getKey() + " x " + e.getValue();
                        p.addChatMessage(new ChatComponentText(row));
                    }

                }
            }
            return null;
        }

    }
}
