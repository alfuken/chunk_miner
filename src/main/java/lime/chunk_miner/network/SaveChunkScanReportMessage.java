package lime.chunk_miner.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lime.chunk_miner.ScanDB;
import lime.chunk_miner.Utils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

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

        class SaveChunkScanReportThread extends Thread {
            private String payload;
            public SaveChunkScanReportThread(String payload) {
                super();
                this.payload = payload;
            }

            public void run() {
                ScanDB.lock();
                ScanDB.initDB();

                HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> map = Utils.mapFromString(payload);

                Connection conn = null;
                PreparedStatement qry = null;
                String sql = "INSERT INTO scan_registry(name, item, x, z, n, oil) VALUES(?,?,?,?,?,?);";
                int count = 0;
                final int batchSize = 1000;
                int xses = 0;

                try
                {
                    conn = DriverManager.getConnection(ScanDB.dbFile());
                    qry = conn.prepareStatement(sql);
                    qry.setQueryTimeout(10);

                    for(HashMap.Entry<Integer, HashMap<Integer, HashMap<String, Integer>>> x_entry : map.entrySet())
                    {
                        int x = x_entry.getKey();
                        xses++;

                        for(HashMap.Entry<Integer, HashMap<String, Integer>> z_entry : x_entry.getValue().entrySet())
                        {
                            int z = z_entry.getKey();

                            for (HashMap.Entry<String, Integer> item_entry : z_entry.getValue().entrySet())
                            {
                                String item = item_entry.getKey();
                                int n = item_entry.getValue();
                                String name = Utils.nameFromString(item);

                                if (!Utils.shouldBeSkipped(name))
                                {
                                    int oil  = 0;
                                    if (name.equals("Natural Gas")
                                        || name.equals("Light Oil")
                                        || name.equals("Heavy Oil")
                                        || name.equals("Raw Oil")
                                        || name.equals("Oil")
                                    ) oil = 1;

                                    qry.setString(1, name);
                                    qry.setString(2, item);
                                    qry.setInt(3, x);
                                    qry.setInt(4, z);
                                    qry.setInt(5, n);
                                    qry.setInt(6, oil);
                                    qry.addBatch();

                                    if(++count % batchSize == 0) {
                                        qry.executeBatch();
                                    }
                                }
                            }
                        }
                    }
                    qry.executeBatch();

                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.out);
                }
                finally
                {
                    if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
                    if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
                }

                double time_taken = (System.currentTimeMillis() - ScanDB.getLock())/1000.0;
                ScanDB.unlock();

                if (xses > 1){
                    ScanDB.player().addChatMessage(new ChatComponentText(String.format("Scan complete in %.2fs, added %d entries to database.", time_taken, count)));
                }
            }
        }

        @Override
        public IMessage onMessage(SaveChunkScanReportMessage message, MessageContext ctx) {
            if (ctx.side.isClient() && message.payload != null){

                if (ScanDB.getLock() > 0)
                {
                    ScanDB.player().addChatMessage(new ChatComponentText("Scan already in progress. Try again later."));
                }
                else
                {
                    new SaveChunkScanReportThread(message.payload.getString("payload")).start();
                }
            }
            return null;
        }

    }

}

/*
*
//                NBTTagCompound nbt = message.payload;
//                int x = nbt.getInteger("x");
//                int z = nbt.getInteger("z");
//                ScanDB.i(i).delete(x, z);
//                NBTTagList list = nbt.getTagList("list", nbt.getId());
//                for (int i = 0; i < list.tagCount(); i++) {
//                    NBTTagCompound list_item = list.getCompoundTagAt(i);
//                    ScanDB.i(i).insert(list_item.getString("item"), x, z, list_item.getInteger("n"));
//                }
*
*
* */