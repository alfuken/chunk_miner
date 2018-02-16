package lime.chunk_miner.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.gui.OreListPane;
import net.minecraft.entity.player.EntityPlayer;

public class OpenScanRegistryMessage implements IMessage {
    public OpenScanRegistryMessage(){}
    @Override public void toBytes(ByteBuf buf) {}
    @Override public void fromBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<OpenScanRegistryMessage, IMessage> {

        @Override
        public IMessage onMessage(OpenScanRegistryMessage message, MessageContext ctx) {
            if (ctx.side.isClient()){
                EntityPlayer p = ChunkMiner.proxy.getPlayer(ctx);
                new OreListPane(p).show();
            }
            return null;
        }

    }
}
