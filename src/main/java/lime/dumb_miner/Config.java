package lime.dumb_miner;

import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

public class Config {
    private static final String CS = "scanner";
    private static final String CM = "miner";
//  public static int recipe_level           = 2;
    public static int work_to_mine           = 10;
    public static int seconds_to_mine        = 10;
    public static int levels_to_mine         = 16;
    public static String[] ignored_materials = {" Dust", "Chipped ", "Flawed ", "Crushed "};
    public static boolean skip_poor_ores     = true;
    public static boolean load_chunks        = false;
    public static boolean inform_gt_chunks   = true;
    public static boolean selfdestruct       = true;
    public static boolean require_redstone   = true;
    public static String  scan_mode          = "optimistic";

    public static void readConfig() {
        Configuration cfg = DumbMiner.config;
        try {
            cfg.load();

            cfg.addCustomCategoryComment(CS, "Scanner configuration");
            ignored_materials = cfg.getStringList("Don't show these materials in scan report", CS, ignored_materials, "List of substrings to ignore when 'Don't show poor ores' is set to 'true'. By default it ignores poor ores and cheap gems. Quarry still mines them though!");
            skip_poor_ores    = cfg.getBoolean("Don't show poor ores in scan report",          CS, skip_poor_ores,    "Don't show poor ores and cheap gems in scan report.");
            inform_gt_chunks  = cfg.getBoolean("Inform about GT Vein chunk",                   CS, inform_gt_chunks,  "If enabled, scanner will notify player if he is in the chunk where GT Vein core is generated (works only if Gregtech is installed).");
            scan_mode         = cfg.getString("Scan mode",                                     CS, scan_mode,         "Ore scanning mode. Optimistic is faster: considers valuable all blocks who's name contains substring 'ore'. Classic is slower: uses the same mechanics as IC2 Ore Scanner.", new String[]{"optimistic", "classic"});

            cfg.addCustomCategoryComment(CM, "Miner configuration");
            work_to_mine      = cfg.getInt("Work to mine",                                     CM, work_to_mine,     1, Integer.MAX_VALUE, "Amount of 'use' work performed on quarry to manually mine the ore.");
            seconds_to_mine   = cfg.getInt("Seconds to mine",                                  CM, seconds_to_mine,  1, Integer.MAX_VALUE, "Amount of seconds between automatic mining when quarry is redstone powered.");
            levels_to_mine    = cfg.getInt("Mine this amount of Y levels",                     CM, levels_to_mine,   0, 255, "Mine N count of Y levels below miner. Set to 0 to mine the whole chunk top to bottom.");
            load_chunks       = cfg.getBoolean("Should quarry act as chunk loader",            CM, load_chunks,      "If enabled, quarry will try to keep the chunk loaded when placed.");
            selfdestruct      = cfg.getBoolean("Self-destruct when done",                      CM, selfdestruct,     "Automatically remove the miner block when it's done mining (it's one-time use anyway, so why bother removing?)");
            require_redstone  = cfg.getBoolean("Require redstone to mine automatically",       CM, require_redstone, "Require redstone signal to be present to mine ore automatically.");
//          recipe_level      = cfg.getInt("Miner recipe difficulty level",                    CM, recipe_level,     1, 6, "'Difficulty' of the miner recipe. 1 - 4x Bronze ingots, 2 - 4x Bronze blocks, 3 - 4x Iron i., 4 - 4x Iron b., 5 - 4x Steel i., 6 - 4x Steel b.");

        } catch (Exception e1) {
            DumbMiner.logger.log(Level.ERROR, "Problem loading config file!", e1);
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }
}
