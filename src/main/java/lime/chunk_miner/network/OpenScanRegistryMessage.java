package lime.chunk_miner.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lime.chunk_miner.ChunkMinerHelpers;
import lime.chunk_miner.gui.OreListPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class OpenScanRegistryMessage implements IMessage {
    public OpenScanRegistryMessage(){}
    private NBTTagCompound payload;
    public OpenScanRegistryMessage(NBTTagCompound payload){
        this.payload = payload;
    }

    @Override public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, payload);
    }

    @Override public void fromBytes(ByteBuf buf) {
        this.payload = ByteBufUtils.readTag(buf);
    }

    public static class Handler implements IMessageHandler<OpenScanRegistryMessage, IMessage> {

        @Override
        public IMessage onMessage(OpenScanRegistryMessage message, MessageContext ctx) {
            if (ctx.side.isClient()){
                EntityClientPlayerMP p = Minecraft.getMinecraft().thePlayer;
                new OreListPane(p, ChunkMinerHelpers.scanDataNBTToMap(p)).show();
            }
            return null;
        }

    }
}

