package lime.chunk_miner.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lime.chunk_miner.ScanDB;
import lime.chunk_miner.Utils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SaveScanReportMessage implements IMessage {
    public SaveScanReportMessage(){}
    private NBTTagList payload;
    public SaveScanReportMessage(NBTTagList payload)
    {
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

    public static class Handler implements IMessageHandler<SaveScanReportMessage, IMessage>
    {

        class SaveChunkScanReportThread extends Thread
        {
            private NBTTagList payload;
            public SaveChunkScanReportThread(NBTTagList payload)
            {
                super();
                this.payload = payload;
            }

            public void run()
            {
                ScanDB.lock();
                ScanDB.initDB();

                Connection conn = null;
                PreparedStatement qry = null;
                String sql = "INSERT INTO scan_registry(name, item, x, z, n, oil, dim) VALUES(?,?,?,?,?,?,?);";
                int count = 0;
                final int batchSize = 400;

                try
                {
                    conn = DriverManager.getConnection(ScanDB.dbFile());
                    qry = conn.prepareStatement(sql);
                    qry.setQueryTimeout(10);

                    for (int i = 0; i < payload.tagCount(); i++) {

                        NBTTagCompound tag = payload.getCompoundTagAt(i);
                        int x = tag.getInteger("x");
                        int z = tag.getInteger("z");
                        ScanDB.delete(x, z);

                        for (Object _item : tag.func_150296_c())
                        {
                            String item = (String)_item;

                            if (!item.equals("x") && !item.equals("z"))
                            {
                                String name = Utils.nameFromString(item);

                                boolean skip = Utils.shouldBeSkipped(name);

                                if (skip)
                                {
                                    NBTTagCompound itemtag = Utils.tagFromString(item);
                                    if (!Utils.isFluidTag(itemtag)){
                                        ItemStack the_item = Utils.itemFromString(item);
                                        if (the_item.getItem() == Items.glowstone_dust){
                                            skip = false;
                                        }
                                    }
                                }

                                if (skip == false)
                                {
                                    int n = tag.getInteger(item);
                                    int oil = (
                                           name.equals("Natural Gas")
                                        || name.equals("Light Oil")
                                        || name.equals("Heavy Oil")
                                        || name.equals("Raw Oil")
                                        || name.equals("Oil")
                                    ) ? 1 : 0;

                                    qry.setString(1, name);
                                    qry.setString(2, item);
                                    qry.setInt(3, x);
                                    qry.setInt(4, z);
                                    qry.setInt(5, n);
                                    qry.setInt(6, oil);
                                    qry.setInt(7,ScanDB.player().dimension);
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

                if (payload.tagCount() > 1){
                    ScanDB.player().addChatMessage(new ChatComponentText(String.format("Scan complete in %.2fs, added %d entries to database.", time_taken, count)));
                }
            }
        }

        @Override
        public IMessage onMessage(SaveScanReportMessage message, MessageContext ctx) {
            if (ctx.side.isClient() && message.payload != null){

                if (ScanDB.getLock() > 0)
                {
                    ScanDB.player().addChatMessage(new ChatComponentText("Another scan is in progress. Try again later."));
                }
                else
                {
                    new SaveChunkScanReportThread(message.payload).start();
                }
            }
            return null;
        }

    }

}

/*
*
//                NBTTagCompound nbt = message.data;
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