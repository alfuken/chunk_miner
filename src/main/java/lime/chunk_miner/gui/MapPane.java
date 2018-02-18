package lime.chunk_miner.gui;

import gminers.glasspane.GlassPane;
import gminers.glasspane.HorzAlignment;
import gminers.glasspane.component.PaneBox;
import gminers.glasspane.component.button.PaneButton;
import gminers.glasspane.component.text.PaneLabel;
import lime.chunk_miner.ScanDB;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.chunk.Chunk;

import java.util.Map;

public class MapPane extends GlassPane {

    public MapPane(final String name){
        final EntityPlayer p = Minecraft.getMinecraft().thePlayer;
        int px = (int)p.posX;
        int pz = (int)p.posZ;
        setRevertAllowed(true);
        setName("MapPane");
        setShadowbox(null);

        add(GuiHelpers.book_background());
        add(GuiHelpers.back_button());

        PaneButton coords_button = new PaneButton("coords");
        coords_button.setWidth(45);
        coords_button.setHeight(15);
        coords_button.setAutoPositionX(true);
        coords_button.setRelativeX(0.5D);
        coords_button.setRelativeXOffset(90);
        coords_button.setY(15);
        coords_button.registerActivationListener(new Runnable() {
            @Override
            public void run() {
                new CoordinatesListPane(name).show();
            }
        });
        add(coords_button);


        PaneBox bg_box;
        for (int bg_x = 0; bg_x <= 12; bg_x++) {
            for (int bg_y = 0; bg_y <= 12; bg_y++) {
                if ((bg_x % 2 == 0 && bg_y % 2 == 0) || (bg_x % 2 == 1 && bg_y % 2 == 1)) {
                    bg_box = background_tile_dark(bg_x, bg_y);
                } else {
                    bg_box = background_tile_light(bg_x, bg_y);
                }

                if (bg_x == 6) bg_box.setWidth(3);
                if (bg_y == 6) bg_box.setHeight(3);
                if (bg_x > 6) bg_box.setRelativeXOffset(bg_box.getRelativeXOffset() - 15 + 3);
                if (bg_y > 6) bg_box.setY(bg_box.getY() - 15 + 3);

//                add(bg_box);
            }
        }

        Chunk chunk = p.worldObj.getChunkFromBlockCoords(px, pz);
        int center_x = chunk.xPosition;
        int center_z = chunk.zPosition;

        boolean center_taken = false;

        Map<Integer, Map<Integer, Integer>> coords = ScanDB.get(name, chunk.xPosition, chunk.zPosition, 31);

        for(Map.Entry<Integer, Map<Integer, Integer>> e : coords.entrySet()){
            int x = e.getKey();
            Map<Integer, Integer> zn = e.getValue();
            for(Map.Entry<Integer, Integer> ee : zn.entrySet()){
                int z = ee.getKey();
                int n = ee.getValue();
                String label = String.valueOf(n)+" @ "+(x*16+8)+":"+(z*16+8);
                int colour = count2colour(n);
                add(cell(x-center_x, z-center_z, colour, label));

                if (x-center_x == 0 && z-center_z == 0) center_taken = true;
            }
        }

        if (!center_taken) add(cell(0, 0, 0xFF000000, "You"));

        PaneLabel label = new PaneLabel(name+"\noccurences map (chunks)");
        label.setAutoResizeWidth(true);
        label.setAlignmentX(HorzAlignment.MIDDLE);
        label.setY(45);
        label.setColor(0xff000000);
        label.setShadow(false);
        add(label);

        int x_lvl_for_hor_line = -113;
        int x_lvl_for_y_coord_legend = 84;
        int y_center_for_y_coord_legend = 180;
        add(hor_line(x_lvl_for_hor_line, y_center_for_y_coord_legend - 75 + 3));
        add(hor_line(x_lvl_for_hor_line, y_center_for_y_coord_legend - 36 + 3));
        add(hor_line(x_lvl_for_hor_line, y_center_for_y_coord_legend      + 3));
        add(hor_line(x_lvl_for_hor_line, y_center_for_y_coord_legend + 36 + 3));
        add(hor_line(x_lvl_for_hor_line, y_center_for_y_coord_legend + 75 + 3));
        add(lbl(pz-(25*16)+"", x_lvl_for_y_coord_legend, y_center_for_y_coord_legend-75));
        add(lbl(pz-(12*16)+"", x_lvl_for_y_coord_legend, y_center_for_y_coord_legend-36));
        add(lbl(pz+"",         x_lvl_for_y_coord_legend, y_center_for_y_coord_legend+1 ));
        add(lbl(pz+(12*16)+"", x_lvl_for_y_coord_legend, y_center_for_y_coord_legend+36));
        add(lbl(pz+(25*16)+"", x_lvl_for_y_coord_legend, y_center_for_y_coord_legend+75));

        int y_lvl_for_ver_line = 80;
        int y_lvl_for_x_coord_legend = 280;
        int x_center_for_x_coord_legend = -13;
        add(ver_line(x_center_for_x_coord_legend - 75 - 4, y_lvl_for_ver_line));
        add(ver_line(x_center_for_x_coord_legend - 36 - 4, y_lvl_for_ver_line));
        add(ver_line(x_center_for_x_coord_legend      - 4, y_lvl_for_ver_line));
        add(ver_line(x_center_for_x_coord_legend + 36 - 4, y_lvl_for_ver_line));
        add(ver_line(x_center_for_x_coord_legend + 75 - 4, y_lvl_for_ver_line));

        add(lbl(px-(25*16)+"", x_center_for_x_coord_legend-75, y_lvl_for_x_coord_legend));
        add(lbl(px-(12*16)+"", x_center_for_x_coord_legend-36, y_lvl_for_x_coord_legend));
        add(lbl(px+"",         x_center_for_x_coord_legend   , y_lvl_for_x_coord_legend));
        add(lbl(px+(12*16)+"", x_center_for_x_coord_legend+36, y_lvl_for_x_coord_legend));
        add(lbl(px+(25*16)+"", x_center_for_x_coord_legend+75, y_lvl_for_x_coord_legend));

        add(lbl("-400", x_center_for_x_coord_legend-75, 80));
        add(lbl("-200", x_center_for_x_coord_legend-36, 80));
        add(lbl("0",    x_center_for_x_coord_legend,    80));
        add(lbl("+200", x_center_for_x_coord_legend+36, 80));
        add(lbl("+400", x_center_for_x_coord_legend+75, 80));

        add(lbl("Colour values", -111, 297));

        int big_cell_y_lvl = 307;
        int big_cell_step = 26;
        int base = -111-big_cell_step;
        add(bigger_cell(base += big_cell_step, big_cell_y_lvl, count2colour(1)));
        add(bigger_cell(base += big_cell_step, big_cell_y_lvl, count2colour(31)));
        add(bigger_cell(base += big_cell_step, big_cell_y_lvl, count2colour(101)));
        add(bigger_cell(base += big_cell_step, big_cell_y_lvl, count2colour(501)));
        add(bigger_cell(base += big_cell_step, big_cell_y_lvl, count2colour(1001)));

        int y_level_for_colour_legend = 316;
        add(lbl("1",     -111, y_level_for_colour_legend));
        add(lbl("30",    -92,  y_level_for_colour_legend));
        add(lbl("100",   -66,  y_level_for_colour_legend));
        add(lbl("500",   -42,  y_level_for_colour_legend));
        add(lbl("1000+", -17,  y_level_for_colour_legend));
    }

    /* ============================================================================================================== */
    /* ============================================================================================================== */
    /* ============================================================================================================== */

    private PaneLabel lbl(String txt, int x, int y){
        PaneLabel label2 = new PaneLabel(txt);
        label2.setAutoResizeWidth(true);
        label2.setAutoPositionX(true);
        label2.setRelativeX(0.5D);
        label2.setRelativeXOffset(x);
        label2.setY(y);
        label2.setColor(0x88000000);
        label2.setShadow(false);
        return label2;
    }

    private PaneBox basicBox(int x, int y){
        return basicBox(x, y, 0xEE000000);
    }

    private PaneBox basicBox(int x, int y, int colour){
        PaneBox box = new PaneBox(colour);
        box.setAutoPositionX(true);
        box.setRelativeX(0.5D);
        box.setRelativeXOffset(x);
        box.setWidth(1);
        box.setHeight(1);
        box.setY(y);
        return box;
    }

    private PaneBox hor_line(int x, int y){
        return hor_line(x, y, 0x44000000);
    }
    private PaneBox hor_line(int x, int y, int colour){
        int width = 193;
        PaneBox box = basicBox(x, y);
        box.setColor(colour);
        box.setWidth(width);
        box.setZIndex(3);
        return box;
    }

    private PaneBox ver_line(int x, int y){
        return ver_line(x, y, 0x44000000);
    }

    private PaneBox ver_line(int x, int y, int colour){
        int height = 206;
        PaneBox box = basicBox(x, y);
        box.setColor(colour);
        box.setHeight(height);
        box.setZIndex(3);
        return box;
    }


    private PaneBox background_tile_light(int x, int y){
        return background_tile(x, y, 0x33FFFFFF);
    }

    private PaneBox background_tile_dark(int x, int y){
        return background_tile(x, y, 0x33000000);
    }

    private PaneBox background_tile(int x, int y, int colour){
        int box_size = 15;
        PaneBox box = basicBox((x*box_size)-105, (y*box_size)+92, colour);
        box.setWidth(box_size);
        box.setHeight(box_size);
        box.setZIndex(0);
        return box;
    }

    private PaneBox cell(int x_offset, int y_offset, int colour, Object label){
        int box_size = 3;
        PaneBox box = basicBox((x_offset*box_size)-18, (y_offset*box_size)+182, colour);
        if (!String.valueOf(label).equals("")) box.setTooltip(String.valueOf(label));
        box.setWidth(box_size);
        box.setHeight(box_size);
        box.setZIndex(1);
        return box;
    }

    private PaneBox cell(int x_offset, int y_offset){
        return cell(x_offset, y_offset, 0xff000000, "");
    }

    private PaneBox cell(int x_offset, int y_offset, int colour){
        return cell(x_offset, y_offset, colour, "");
    }

    private PaneBox bigger_cell(int x_offset, int y_offset, int colour){
        PaneBox box = basicBox(x_offset, y_offset, colour);
        box.setWidth(26);
        box.setHeight(5);
        box.setZIndex(1);
        return box;
    }

    private int count2colour(int n){
             if (n >= 1   && n <= 30)        return 0x20000000;
        else if (n >= 31  && n <= 100)       return 0x50000000;
        else if (n >= 101 && n <= 500)       return 0x90000000;
        else if (n >= 501 && n <= 1000)      return 0xb0000000;
        else if (n >  1000)                  return 0xe0000000;
        else                                 return 0x00ffffff;
    }

//    private int count2colour(int n){
//             if (n >= 1   && n <= 20)        return 0x99e22f02;
//        else if (n >= 21  && n <= 50)        return 0x99ff9d00;
//        else if (n >= 51  && n <= 100)       return 0x99ffff00;
//        else if (n >= 101 && n <= 300)       return 0x9914d802;
//        else if (n >= 301 && n <= 500)       return 0x9902e6f2;
//        else if (n >= 501 && n <= 1000)      return 0x990245ff;
//        else if (n >  1000)                  return 0x99ff01d0;
//        else                                 return 0x99ffffff;
//    }

    private int oilColour(String name){
             if (name.equals("Natural Gas")) return 0x9980daeb;
        else if (name.equals("Light Oil"))   return 0x99ffdd2b;
        else if (name.equals("Heavy Oil"))   return 0x99c47c0f;
        else if (name.equals("Raw Oil"))     return 0x99773e01;
        else if (name.equals("Oil"))         return 0x99000000;
        else return Integer.decode(String.format("0xEE%06X", (0xFFFFFF & name.hashCode())));
    }

    private boolean isOil(String name){
        return (name.equals("Natural Gas") || name.equals("Light Oil") || name.equals("Heavy Oil") || name.equals("Raw Oil") || name.equals("Oil"));
    }
}
