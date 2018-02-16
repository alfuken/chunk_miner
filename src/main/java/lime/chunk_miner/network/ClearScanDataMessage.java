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
                    Connection conn = null;
                    PreparedStatement qry  = null;
                    String            sql  = "DELETE FROM scan_registry WHERE name like ?";

                    try
                    {
                        conn = DriverManager.getConnection(ScanDB.p(player).db_file);
                        qry = conn.prepareStatement(sql);
                        qry.setString(1, "%"+m+"%");
                        qry.executeUpdate();
                    }
                    catch (SQLException e)
                    {
                        e.printStackTrace(System.out);
                    }
                    finally
                    {
                        if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
                        if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
                    }
                }
                player.addChatMessage(new ChatComponentText("Scan data was cleared."));
            }
            return null;
        }
    }
}
