package lime.chunk_miner;

import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

public class Config {
    private static final String CS = "Scanner";

    public static String[] ignored_materials = {"Chipped ", "Flawed ", "Crushed ", "Impure Pile of ", " Dust"};
    public static String  scan_mode          = "optimistic";
    public static int area_scan_radius       = 3;

    private static final String CM = "Miner";

    public static int work_to_mine           = 5;
    public static int seconds_to_mine        = 10;
    public static boolean load_chunks        = true;
    public static boolean selfdestruct       = true;
    public static boolean require_redstone   = true;

    public static void readConfig() {
        Configuration cfg = ChunkMiner.config;
        try {
            cfg.load();

            ignored_materials = cfg.getStringList("Ignore these materials in scan report",     CS, ignored_materials, "List of substrings to ignore when 'Don't show poor ores' is set to 'true'. By default it ignores poor ores and cheap gems. Quarry still mines them though!");
            scan_mode         = cfg.getString("Scan mode",                                     CS, scan_mode,         "Ore scanning mode. Optimistic is fastest: considers valuable all blocks who's internal (unlocalized) name contains substring 'ore'. Classic is slower: uses the same mechanics as IC2 Ore Scanner.", new String[]{"optimistic", "classic"});
            area_scan_radius  = cfg.getInt("Area scan radius in chunks",                       CS, area_scan_radius, 1, 32, "Area scan radius in chunks. Value of 1 scans area 3x3 chunks. Value of 3 scans 7x7 (49) chunks. Higher the number, higher the load on CPU.");

            work_to_mine      = cfg.getInt("Work to mine",                                     CM, work_to_mine,     1, Integer.MAX_VALUE, "Amount of 'use' work performed on quarry to manually mine the ore.");
            seconds_to_mine   = cfg.getInt("Seconds to mine",                                  CM, seconds_to_mine,  1, Integer.MAX_VALUE, "Amount of seconds between automatic mining when quarry is redstone powered.");
            load_chunks       = cfg.getBoolean("Should quarry act as chunk loader",            CM, load_chunks,      "If enabled, quarry will try to keep the chunk loaded when placed.");
            selfdestruct      = cfg.getBoolean("Self-destruct when done",                      CM, selfdestruct,     "Automatically remove the miner block when it's done mining (it's one-time use anyway, so why bother removing?)");
            require_redstone  = cfg.getBoolean("Require redstone to mine automatically",       CM, require_redstone, "Require redstone signal to be present to mine ore automatically.");

        } catch (Exception e) {
            ChunkMiner.logger.log(Level.ERROR, "Problem loading config file!", e);
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }
}
