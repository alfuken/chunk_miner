package lime.dumb_miner;

import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

public class Config {
    private static final String CAT = "general";
    public static int work_to_mine = 7;
    public static int seconds_to_mine = 10;
    public static int mine_to_y = 0;
    public static boolean skip_poor_ores = true;
    public static String[] ignored_materials = {" Dust", "Chipped ", "Flawed ", "Crushed "};
    public static boolean load_chunks = true;
    public static boolean inform_gt_chunks = true;

    public static void readConfig() {
        Configuration cfg = DumbMiner.config;
        try {
            cfg.load();
            cfg.addCustomCategoryComment(CAT, "General configuration");
            work_to_mine = cfg.getInt("Work to mine",                                         CAT, work_to_mine,    1, Integer.MAX_VALUE, "Amount of 'use' work performed on quarry to manually mine the ore.");
            seconds_to_mine = cfg.getInt("Seconds to mine",                                   CAT, seconds_to_mine, 1, Integer.MAX_VALUE, "Amount of seconds between automatic mining when quarry is redstone powered.");
            mine_to_y = cfg.getInt("Mine to this Y level",                                    CAT, mine_to_y,       0, 255, "Mine until this level is reached.");
            skip_poor_ores = cfg.getBoolean("Don't show poor ores in scan report",            CAT, skip_poor_ores,          "Don't show poor ores and cheap gems in scan report.");
            ignored_materials = cfg.getStringList("Don't show this materials in scan report", CAT, ignored_materials,       "List of substrings to ignore when 'Don't show poor ores' is set to 'true'. By default it ignores poor ores and cheap gems. Quarry still mines them though!");
            load_chunks = cfg.getBoolean("Should quarry act as chunk loader",                 CAT, load_chunks,             "If enabled, quarry will try to keep the chunk loaded when placed.");
            inform_gt_chunks = cfg.getBoolean("Inform about GT Vein chunk",                   CAT, inform_gt_chunks,        "If enabled, scanner will notify player if he is in the chunk where GT Vein core is generated (works only if Gregtech is installed).");
        } catch (Exception e1) {
            DumbMiner.logger.log(Level.ERROR, "Problem loading config file!", e1);
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }

}
