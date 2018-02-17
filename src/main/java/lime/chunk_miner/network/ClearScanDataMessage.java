package lime.chunk_miner.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lime.chunk_miner.ChunkMiner;
import lime.chunk_miner.Config;
import lime.chunk_miner.ScanDB;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClearScanDataMessage implements IMessage {
    public ClearScanDataMessage(){}

    @Override public void toBytes(ByteBuf buf) {}
    @Override public void fromBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<ClearScanDataMessage, IMessage> {
        @Override
        public IMessage onMessage(ClearScanDataMessage message, MessageContext ctx) {
            if (ctx.side.isClient()){
                EntityPlayer player = ChunkMiner.proxy.getPlayer(ctx);
                for(String m : Config.ignored_materials){
                    ScanDB.deleteBadScanResults(m);
                }
                player.addChatMessage(new ChatComponentText("Scan data was cleared."));
            }
            return null;
        }
    }
}
