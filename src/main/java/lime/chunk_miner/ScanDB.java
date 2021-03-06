package lime.chunk_miner;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScanDB {
    private static long locked_at = 0;
    private static boolean dbInitialized = false;

    public ScanDB(){}

    public static void initDB(){
        if (!dbInitialized){
            if (!ScanDB.isDBSetUp()) {
                ScanDB.setupPlayerDB();
            }
            ScanDB.migratePlayerDB1();
//            ScanDB.migratePlayerDB2();
            dbInitialized = true;
        }
    }

    public static EntityPlayer player(){
        return Minecraft.getMinecraft().thePlayer;
    }

    public static String dbFile(){
        return "jdbc:sqlite:"+playerDataFile().toString();
    }

    /*
    *
    * =============================== Instance methods ===============================
    *
    * */

    public static void lock(){locked_at = System.currentTimeMillis();}
    public static void unlock(){locked_at = 0;}
    public static long getLock(){return locked_at;}

    private static File playerDataFile(){
        File f = new File(
            "./" + ChunkMiner.MODID + File.separator +
            Minecraft.getMinecraft().func_147104_D().serverName + File.separator +
            player().getDisplayName()+".sqlite"
        );

        if (!f.exists())
        {
            try
            {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            catch (IOException io)
            {
                io.printStackTrace(System.out);
            }
        }
        return f;
    }

    public static List<String> get_ore_names(){
        return get_oil_or_ore_names(0);
    }

    public static List<String> get_oil_names(){
        return get_oil_or_ore_names(1);
    }

    private static List<String> get_oil_or_ore_names(int oil)
    {
        initDB();
        Connection         conn = null;
        PreparedStatement  qry  = null;
        ResultSet          r    = null;
        String             sql  = "SELECT DISTINCT name FROM scan_registry WHERE oil = ? AND dim = ? ORDER BY name;";
        List<String>       ret  = new ArrayList<String>();
        try
        {
            conn = DriverManager.getConnection(dbFile());
            qry = conn.prepareStatement(sql);
            qry.setInt(1, oil);
            qry.setInt(2, player().dimension);
            r = qry.executeQuery();

            while (r.next())
            {
                ret.add(r.getString("name"));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
        finally
        {
            if (r    != null) {try { r.close();    } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
        }
        return ret;
    }

    public static ArrayList<int[]> get(String name, int x, int z, int range)
    {
        initDB();
        Connection         conn = null;
        PreparedStatement  qry  = null;
        ResultSet          r    = null;
        String             sql  = "SELECT * FROM scan_registry WHERE name = ? AND x BETWEEN ? AND ? AND z BETWEEN ? and ? AND dim = ?;";
        ArrayList<int[]> ret = new ArrayList<int[]>();

        try
        {
            conn = DriverManager.getConnection(dbFile());
            qry = conn.prepareStatement(sql);
            qry.setString(1,name);
            qry.setInt(2,x-range);
            qry.setInt(3,x+range);
            qry.setInt(4,z-range);
            qry.setInt(5,z+range);
            qry.setInt(6,player().dimension);
            r = qry.executeQuery();

            while (r.next())
            {
                ret.add(new int[]{r.getInt("x"), r.getInt("z"), r.getInt("n")});
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
        finally
        {
            if (r    != null) {try { r.close();    } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
        }

        return ret;
    }

    public static ArrayList<int[]> get_grey_area(int x, int z, int range)
    {
        initDB();
        Connection         conn = null;
        PreparedStatement  qry  = null;
        ResultSet          r    = null;
        String             sql  = "SELECT DISTINCT x, z FROM scan_registry WHERE x BETWEEN ? AND ? AND z BETWEEN ? and ? AND dim = ?;";
        ArrayList<int[]> ret = new ArrayList<int[]>();

        try
        {
            conn = DriverManager.getConnection(dbFile());
            qry = conn.prepareStatement(sql);
            qry.setInt(1,x-range);
            qry.setInt(2,x+range);
            qry.setInt(3,z-range);
            qry.setInt(4,z+range);
            qry.setInt(5,player().dimension);
            r = qry.executeQuery();

            while (r.next())
            {
                ret.add(new int[]{r.getInt("x"), r.getInt("z")});
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
        finally
        {
            if (r    != null) {try { r.close();    } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
        }

        return ret;
    }

    public static List<String> get(String name)
    {
        initDB();
        Connection        conn = null;
        PreparedStatement qry  = null;
        ResultSet         r    = null;
        String            sql  = "SELECT * FROM scan_registry WHERE name = ? AND dim = ? ORDER BY n DESC;";
        List<String>      ret  = new ArrayList<String>();

        try
        {
            conn = DriverManager.getConnection(dbFile());
            qry = conn.prepareStatement(sql);
            qry.setString(1,name);
            qry.setInt(2,player().dimension);
            r = qry.executeQuery();

            while (r.next())
            {
                String row = (r.getInt("x")*16+8)+":"+(r.getInt("z")*16+8)+" x "+r.getInt("n");
                if (!ret.contains(row)) ret.add(row);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
        finally
        {
            if (r    != null) {try { r.close();    } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
        }
        return ret;
    }

    public static void delete(int x, int z)
    {
        initDB();
        Connection conn = null;
        PreparedStatement qry = null;
        String sql = "DELETE FROM scan_registry WHERE x = ? AND z = ? AND dim = ?";

        try
        {
            conn = DriverManager.getConnection(dbFile());
            qry = conn.prepareStatement(sql);
            qry.setInt(1, x);
            qry.setInt(2, z);
            qry.setInt(3,player().dimension);
            qry.executeUpdate();
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
    }

    public static void deleteBadScanResults(String string)
    {
        initDB();
        Connection conn = null;
        PreparedStatement qry = null;
        String sql = "DELETE FROM scan_registry WHERE name like ?";

        try
        {
            conn = DriverManager.getConnection(dbFile());
            qry = conn.prepareStatement(sql);
            qry.setString(1, "%"+string+"%");
            qry.executeUpdate();
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
    }

    public static void insert(String item, int x, int z, int n)
    {
        initDB();
        Connection        conn = null;
        PreparedStatement qry  = null;
        String            sql  = "INSERT INTO scan_registry(name, item, x, z, n, oil, dim) VALUES(?,?,?,?,?,?,?);";
        String            name = Utils.nameFromString(item);
        int               oil  = 0;

        if (Utils.shouldBeSkipped(name)) return;

        if (name.equals("Natural Gas") ||
            name.equals("Light Oil") ||
            name.equals("Heavy Oil") ||
            name.equals("Raw Oil") ||
            name.equals("Oil")
        ) oil = 1;

        try
        {
            conn = DriverManager.getConnection(dbFile());
            qry = conn.prepareStatement(sql);
            qry.setString(1, name);
            qry.setString(2, item);
            qry.setInt(3, x);
            qry.setInt(4, z);
            qry.setInt(5, n);
            qry.setInt(6, oil);
            qry.setInt(7,player().dimension);
            qry.executeUpdate();
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
    }

    private static boolean isDBSetUp()
    {
        Connection conn   = null;
        Statement  qry    = null;
        ResultSet  r      = null;
        String     sql    = "SELECT count(*) AS count FROM sqlite_master WHERE type='table' AND name='scan_registry';";
        boolean    set_up = false;

        try
        {
            conn = DriverManager.getConnection(dbFile());
            qry = conn.createStatement();
            r = qry.executeQuery(sql);
            if (r.getInt("count") == 1) set_up = true;
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
        finally
        {
            if (r    != null) {try { r.close();    } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
        }

        return set_up;
    }

    private static void setupPlayerDB()
    {
        Connection conn = null;
        Statement qry = null;
        String sql = "CREATE TABLE IF NOT EXISTS scan_registry (" +
            " id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " name TEXT NOT NULL," +
            " item TEXT NOT NULL," +
            " x INTEGER NOT NULL," +
            " z INTEGER NOT NULL," +
            " n INTEGER NOT NULL," +
            " oil INTEGER default 0," +
            " dim INTEGER default 0" +
        ");"+

        " CREATE INDEX name_scan_registry_idx"+
        " ON scan_registry (name);"+

        " CREATE INDEX oil_scan_registry_idx"+
        " ON scan_registry (oil);"+

        " CREATE INDEX main_scan_registry_idx"+
        " ON scan_registry (name, x, z);"+

        " CREATE INDEX oil_scan_registry_idx"+
        " ON scan_registry (oil, x, z);";

        try {
            conn = DriverManager.getConnection(dbFile());
            qry = conn.createStatement();
            qry.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        } finally {
            if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
        }

    }


    private static void migratePlayerDB1()
    {
        Connection conn = null;
        Statement qry = null;
        String sql = "ALTER TABLE scan_registry ADD COLUMN dim INTEGER DEFAULT 0;"+

                " CREATE INDEX name_scan_registry_idx2"+
                " ON scan_registry (name, dim);"+

                " CREATE INDEX oil_scan_registry_idx2"+
                " ON scan_registry (oil, dim);"+

                " CREATE INDEX main_scan_registry_idx2"+
                " ON scan_registry (name, x, z, dim);"+

                " CREATE INDEX oil_scan_registry_idx2"+
                " ON scan_registry (oil, x, z, dim);" +

                " UPDATE scan_registry SET dim = 0;";

        try {
            conn = DriverManager.getConnection(dbFile());
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rset = md.getColumns(null, null, "scan_registry", null);

            boolean got_dim = false;
            while (rset.next())
            {
                if (rset.getString(4).equals("dim")) got_dim = true;
            }
            rset.close();

            if (!got_dim)
            {
                qry = conn.createStatement();
                qry.execute(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        } finally {
            if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
        }
    }


//    private static void migratePlayerDB2()
//    {
//        Connection conn = null;
//        Statement qry = null;
//        String sql = "CREATE TABLE IF NOT EXISTS kv_store (" +
//                     " key TEXT PRIMARY KEY," +
//                     " int INTEGER," +
//                     " float REAL," +
//                     " text TEXT," +
//                     " blob BLOB" +
//                     ");"+
//                     "CREATE INDEX key_kv_store_idx "+
//                     " ON kv_store (key);";
//        try {
//            conn = DriverManager.getConnection(dbFile());
//            qry = conn.createStatement();
//            qry.execute(sql);
//        } catch (SQLException e) {
//            e.printStackTrace(System.out);
//        } finally {
//            if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
//            if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
//        }
//    }


}













/*




Structure:

{
  item_name: {
    "item": tag,
    "coords": [
      {
        "x": {
          "y": count
        }
      }
    ]
  }
}





//    String[] keys(){
//        Collection<String> ret = new ArrayList<String>();
//        for (Object _key : data.func_150296_c()) {
//            ret.add((String)_key);
//        }
//        return new TreeSet<String>(ret).toArray(new String[ret.size()]);
//    }
//
//    public NBTTagCompound get(String key){
//        return this.data.getCompoundTag(key);
//    }
//
//    public void set(String name, int count, int x, int y){
//        NBTTagCompound a_record = get(name);
//        NBTTagCompound coords = a_record.getCompoundTag("coords");
//
//    }




    public ScanDB(EntityPlayer player){
        this.player = player;
        setupPlayerDB();
//        read();

//        for (Object _coord : data.func_150296_c()) {
//            coord = (String)_coord;
//            ret.put(coord, data.getCompoundTag(coord));
//        }

    }




//    void read(){
//        NBTTagCompound tag = new NBTTagCompound();
//
//        try {
//            tag = CompressedStreamTools.read(playerDataFile());
//        } catch (IOException io){
//            io.printStackTrace();
//        }
//
//        this.data = tag;
//    }
//
//    void write(){
//        write(this.data);
//    }
//
//    void write(NBTTagCompound tag){
//        File f = playerDataFile();
//
//        try {
//            CompressedStreamTools.write(tag, f);
//        } catch (IOException io){
//            io.printStackTrace();
//        }
//    }
//
//    void clear() {
//        write(new NBTTagCompound());
//    }



        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);

        NBTTagCompound tag = new NBTTagCompound();
        itemStack.writeToNBT(tag);

        try {
            CompressedStreamTools.write(tag, os);
        } catch (IOException io){
            io.printStackTrace();
            os.close();
        }

        String to_s = baos.toString();
        System.out.println(to_s);

//        String base = DatatypeConverter.printBase64Binary(baos.toByteArray());
//        System.out.println(base);

        NBTTagCompound otag = new NBTTagCompound();

        ByteArrayInputStream bais = new ByteArrayInputStream(to_s.getBytes());
        DataInputStream is = new DataInputStream(bais);
        try {
            otag = CompressedStreamTools.read(is);
        } catch (IOException io){
            io.printStackTrace();
        }
        System.out.println("============== 1:");
        System.out.println(ItemStack.loadItemStackFromNBT(otag).getDisplayName());
        System.out.println("--------------");


//        ByteArrayInputStream bais3 = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(base));
//        DataInputStream is3 = new DataInputStream(bais3);
//        try {
//            otag = CompressedStreamTools.read(is3);
//        } catch (IOException io){
//            io.printStackTrace();
//        }


 */