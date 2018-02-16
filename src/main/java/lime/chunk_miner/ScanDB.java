package lime.chunk_miner;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanDB {
    private static ScanDB instance;
    private EntityPlayer player;
    public String db_file;
    public long locked_at;

    public ScanDB(){}
    public ScanDB(EntityPlayer player){
        this.player = player;
        // or just Minecraft.getMinecraft().thePlayer
        this.db_file = "jdbc:sqlite:"+playerDataFile().toString();
    }

    public static ScanDB p(EntityPlayer p){
        if (!p.getEntityWorld().isRemote) return new ScanDB();

        if (ScanDB.instance == null || (!p.getDisplayName().equals(ScanDB.instance.player.getDisplayName()))) {
            ScanDB.instance = new ScanDB(p);
            boolean set_up = ScanDB.instance.isDBSetUp();
            if (!set_up) ScanDB.instance.setupPlayerDB();
        }

        return ScanDB.instance;
    }

    /*
    *
    * =============================== Instance methods ===============================
    *
    * */

    public void lock(){this.locked_at = System.currentTimeMillis();}
    public void unlock(){this.locked_at = 0;}

    private File playerDataFile(){
        File f = new File(
            "./" + ChunkMiner.MODID + File.separator +
            Minecraft.getMinecraft().func_147104_D().serverName + File.separator +
            player.getDisplayName()+".sqlite"
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

    public List<String> get_ore_names(){
        return get_oil_or_ore_names(0);
    }

    public List<String> get_oil_names(){
        return get_oil_or_ore_names(1);
    }

    public List<String> get_oil_or_ore_names(int oil)
    {
        Connection         conn = null;
        PreparedStatement  qry  = null;
        ResultSet          r    = null;
        String             sql  = "SELECT DISTINCT name FROM scan_registry WHERE oil = ? ORDER BY name;";
        List<String>       ret  = new ArrayList<String>();
        try
        {
            conn = DriverManager.getConnection(this.db_file);
            qry = conn.prepareStatement(sql);
            qry.setInt(1, oil);
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
            if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (r    != null) {try { r.close();    } catch (SQLException e) { e.printStackTrace(System.out); }}
        }
        return ret;
    }

    /*
    *
    * Result format: {
    *   x1: {
    *     z1: n1,
    *     z2: n2,
    *     z3: n3
    *   },
    *   x2: {
    *     z1: n1,
    *     z2: n2,
    *     z3: n3
    *   }
    * }
    *
    * */
    public Map<Integer, Map<Integer, Integer>> get(String name, int x, int z, int range){
        Connection         conn = null;
        PreparedStatement  qry  = null;
        ResultSet          r    = null;
        String             sql  = "SELECT * FROM scan_registry WHERE name = ? AND x BETWEEN ? AND ? AND z BETWEEN ? and ?;";
        Map<Integer, Map<Integer, Integer>> map = new HashMap<Integer, Map<Integer, Integer>>();

        try
        {
            conn = DriverManager.getConnection(this.db_file);
            qry = conn.prepareStatement(sql);
            qry.setString(1,name);
            qry.setInt(   2,x-range);
            qry.setInt(   3,x+range);
            qry.setInt(   4,z-range);
            qry.setInt(   5,z+range);
            r = qry.executeQuery();

            while (r.next())
            {
                Map<Integer, Integer> map_x = map.get(r.getInt("x"));
                if (map_x == null)    map_x = new HashMap<Integer, Integer>();

                    map_x.put(r.getInt("z"), r.getInt("n"));

                map.put(r.getInt("x"), map_x);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
        finally
        {
            if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (r    != null) {try { r.close();    } catch (SQLException e) { e.printStackTrace(System.out); }}
        }

        return map;
    }

    public List<String> get(String name){
        Connection        conn = null;
        PreparedStatement qry  = null;
        ResultSet         r    = null;
        String            sql  = "SELECT * FROM scan_registry WHERE name = ? ORDER BY n DESC;";
        List<String>      ret  = new ArrayList<String>();

        try
        {
            conn = DriverManager.getConnection(this.db_file);
            qry = conn.prepareStatement(sql);
            qry.setString(1,name);
            r = qry.executeQuery();

            while (r.next())
            {
                ret.add((r.getInt("x")*16+8)+":"+(r.getInt("z")*16+8)+" x "+r.getInt("n"));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
        finally
        {
            if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (r    != null) {try { r.close();    } catch (SQLException e) { e.printStackTrace(System.out); }}
        }
        return ret;
    }

    public void delete(int x, int z){
        Connection        conn = null;
        PreparedStatement qry  = null;
        String            sql  = "DELETE FROM scan_registry WHERE x = ? AND z = ?";

        try
        {
            conn = DriverManager.getConnection(this.db_file);
            qry = conn.prepareStatement(sql);
            qry.setInt(1, x);
            qry.setInt(2, z);
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

    public void insert(String item, int x, int z, int n){
        Connection        conn = null;
        PreparedStatement qry  = null;
        String            sql  = "INSERT INTO scan_registry(name, item, x, z, n, oil) VALUES(?,?,?,?,?,?);";
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
            conn = DriverManager.getConnection(this.db_file);
            qry = conn.prepareStatement(sql);
            qry.setString(1, name);
            qry.setString(2, item);
            qry.setInt(3, x);
            qry.setInt(4, z);
            qry.setInt(5, n);
            qry.setInt(6, oil);
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

    private boolean isDBSetUp(){
        Connection conn   = null;
        Statement  qry    = null;
        ResultSet  r      = null;
        String     sql    = "SELECT count(*) AS count FROM sqlite_master WHERE type='table' AND name='scan_registry';";
        boolean    set_up = false;

        try
        {
            conn = DriverManager.getConnection(this.db_file);
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
            if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (r    != null) {try { r.close();    } catch (SQLException e) { e.printStackTrace(System.out); }}
        }

        return set_up;
    }

    private void setupPlayerDB() {
        Connection conn = null;
        Statement qry = null;
        String     sql = "CREATE TABLE IF NOT EXISTS scan_registry (" +
            " id integer PRIMARY KEY AUTOINCREMENT," +
            " name varchar NOT NULL," +
            " item text NOT NULL," +
            " x integer NOT NULL," +
            " z integer NOT NULL," +
            " n integer NOT NULL," +
            " oil integer default 0" +
        ");"+

        " CREATE INDEX name_scan_registry_idx"+
        " ON scan_registry (name);"+

        " CREATE INDEX main_scan_registry_idx"+
        " ON scan_registry (name, x, z);"+

        " CREATE INDEX oil_scan_registry_idx"+
        " ON scan_registry (oil, x, z);";

        try {
            conn = DriverManager.getConnection(this.db_file);
            qry = conn.createStatement();
            qry.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        } finally {
            if (conn != null) {try { conn.close(); } catch (SQLException e) { e.printStackTrace(System.out); }}
            if (qry  != null) {try { qry.close();  } catch (SQLException e) { e.printStackTrace(System.out); }}
        }

    }

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