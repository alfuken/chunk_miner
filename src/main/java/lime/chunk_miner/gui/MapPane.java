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

import java.awt.*;
import java.util.ArrayList;

public class MapPane extends GlassPane {

    public ArrayList<PaneBox> cells = new ArrayList<PaneBox>();
    public String oreName = "";
    public ArrayList<String> oilNames;

    public static int min = 1;
    public static void setMin(int v){
        if (v < 1) v = 1;
        if (v > 1000) v = 1000;
        min = v;
    }

    public static int scale = 3;
    public static void setScale(int v){
        if (v < 1) v = 1;
        if (v > 10) v = 10;
        scale = v;
    }

    public MapPane(final String oreName){
        this.oreName = oreName;
        setRevertAllowed(true);
        setName("MapPane");
        setShadowbox(null);
        add(GuiHelpers.book_background());
        add(GuiHelpers.back_button());
        add_grid_and_labels();
        add(coords_button(this.oreName));
        render_ores();
    }

    public MapPane(final ArrayList<String> oilNames){
        this.oilNames = oilNames;
        setRevertAllowed(true);
        setName("MapPane");
        setShadowbox(null);
        add(GuiHelpers.book_background());
        add(GuiHelpers.back_button());
        add_grid_and_labels();
        render_oils();
    }

    /* ============================================================================================================== */
    /* ============================================================================================================== */
    /* ============================================================================================================== */



    void render_oils(){
        final EntityPlayer p = Minecraft.getMinecraft().thePlayer;
        Chunk chunk = p.worldObj.getChunkFromBlockCoords((int)p.posX, (int)p.posZ);
        int center_x = chunk.xPosition;
        int center_z = chunk.zPosition;


        for (String name : oilNames)
        {
            ArrayList<int[]> coords = ScanDB.get(name, chunk.xPosition, chunk.zPosition, 31*3/scale);
            for(int[] e : coords)
            {
                int x = e[0];
                int z = e[1];
                int n = e[2];
                if (n >= min){
                    add_cell(x-center_x, z-center_z, count2colour(n), n, name);
                }
            }
        }
    }

    void render_ores(){
        render_greys();
        render_cells();

        PaneLabel label = new PaneLabel(this.oreName+"\noccurences map (chunks)");
        label.setAutoResizeWidth(true);
        label.setAlignmentX(HorzAlignment.MIDDLE);
        label.setY(45);
        label.setColor(0xff000000);
        label.setShadow(false);
        add(label);
    }

    PaneButton coords_button(final String name){
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
        return coords_button;
    }

    void render_cells(){
        final EntityPlayer p = Minecraft.getMinecraft().thePlayer;
        int px = (int)p.posX;
        int pz = (int)p.posZ;
        Chunk chunk = p.worldObj.getChunkFromBlockCoords(px, pz);
        ArrayList<int[]> coords = ScanDB.get(this.oreName, chunk.xPosition, chunk.zPosition, 31);

        int center_x = chunk.xPosition;
        int center_z = chunk.zPosition;

        boolean center_taken = false;

        for(int[] e : coords){
            int x = e[0];
            int z = e[1];
            int n = e[2];
            add_cell(x-center_x, z-center_z, count2colour(n), String.valueOf(n)+" @ "+(x*16+8)+":"+(z*16+8), n, "");

            if (x-center_x == 0 && z-center_z == 0) center_taken = true;
        }

        if (!center_taken) add_cell(0, 0, 0xFF000000, "You");
    }

    void render_greys(){
        final EntityPlayer p = Minecraft.getMinecraft().thePlayer;
        int px = (int)p.posX;
        int pz = (int)p.posZ;
        Chunk chunk = p.worldObj.getChunkFromBlockCoords(px, pz);
        ArrayList<int[]> coords = ScanDB.get_grey_area(chunk.xPosition, chunk.zPosition, 31);

        int center_x = chunk.xPosition;
        int center_z = chunk.zPosition;

        for(int[] e : coords){
            int x = e[0];
            int z = e[1];
            add_cell(x-center_x, z-center_z);
        }
    }

    void hide_cells(){
        if (!cells.isEmpty())
        {
            for (PaneBox cell : cells) {
                if (Integer.parseInt(cell.getMetadata("n")) > min && oilNames.contains(cell.getMetadata("name")))
                {
//                    cell.setColor(Integer.parseInt(cell.getMetadata("c")));
                    cell.setVisible(true);
                }
                else
                {
//                    cell.setColor(0x00000000);
                    cell.setVisible(false);
                }
            }
        }
    }

    void add_cell(int x, int z){
        add_cell(x, z, 0x22000000);
    }

    void add_cell(int x, int z, int colour){
        add_cell(x, z, colour, "");
    }

    void add_cell(int x, int z, int colour, String label){
        add_cell(x, z, colour, label, 0, "");
    }

    void add_cell(int x, int z, int colour, int n, String name){
        add_cell(x, z, colour, n+" "+name, n, name);
    }

    void add_cell(int x, int z, int colour, String label, int n, String name){
        PaneBox cell = cell(x, z, colour, label);
        cell.putMetadata("n", String.valueOf(n));
        cell.putMetadata("name", name);
        cell.putMetadata("c", String.valueOf(colour));
        cells.add(cell);
        add(cell);
    }

    private void add_grid_and_labels(){
        final EntityPlayer p = Minecraft.getMinecraft().thePlayer;
        int px = (int)p.posX;
        int pz = (int)p.posZ;

        int x_lvl_for_hor_line = -113;
        int x_lvl_for_y_coord_legend = 84;
        int y_center_for_y_coord_legend = 180;
        add(hor_line(x_lvl_for_hor_line, y_center_for_y_coord_legend - 75 + 3));
        add(hor_line(x_lvl_for_hor_line, y_center_for_y_coord_legend - 36 + 3));
        add(hor_line(x_lvl_for_hor_line, y_center_for_y_coord_legend      + 3, 0x88000000));
        add(hor_line(x_lvl_for_hor_line, y_center_for_y_coord_legend + 36 + 3));
        add(hor_line(x_lvl_for_hor_line, y_center_for_y_coord_legend + 75 + 3));
        add(lbl(pz-(int)Math.ceil(25*16*3/scale)+"", x_lvl_for_y_coord_legend, y_center_for_y_coord_legend-75));
        add(lbl(pz-(int)Math.ceil(12*16*3/scale)+"", x_lvl_for_y_coord_legend, y_center_for_y_coord_legend-36));
        add(lbl(pz+"",         x_lvl_for_y_coord_legend, y_center_for_y_coord_legend+1 ));
        add(lbl(pz+(int)Math.ceil(12*16*3/scale)+"", x_lvl_for_y_coord_legend, y_center_for_y_coord_legend+36));
        add(lbl(pz+(int)Math.ceil(25*16*3/scale)+"", x_lvl_for_y_coord_legend, y_center_for_y_coord_legend+75));

        int y_lvl_for_ver_line = 80;
        int y_lvl_for_x_coord_legend = 280;
        int x_center_for_x_coord_legend = -13;
        add(ver_line(x_center_for_x_coord_legend - 75 - 4, y_lvl_for_ver_line));
        add(ver_line(x_center_for_x_coord_legend - 36 - 4, y_lvl_for_ver_line));
        add(ver_line(x_center_for_x_coord_legend      - 4, y_lvl_for_ver_line, 0x88000000));
        add(ver_line(x_center_for_x_coord_legend + 36 - 4, y_lvl_for_ver_line));
        add(ver_line(x_center_for_x_coord_legend + 75 - 4, y_lvl_for_ver_line));

        add(lbl(px-(int)Math.ceil(25*16*3/scale)+"", x_center_for_x_coord_legend-75, y_lvl_for_x_coord_legend));
        add(lbl(px-(int)Math.ceil(12*16*3/scale)+"", x_center_for_x_coord_legend-36, y_lvl_for_x_coord_legend));
        add(lbl(px+"",         x_center_for_x_coord_legend   , y_lvl_for_x_coord_legend));
        add(lbl(px+(int)Math.ceil(12*16*3/scale)+"", x_center_for_x_coord_legend+36, y_lvl_for_x_coord_legend));
        add(lbl(px+(int)Math.ceil(25*16*3/scale)+"", x_center_for_x_coord_legend+75, y_lvl_for_x_coord_legend));

        add(lbl("-"+(int)Math.ceil(400*3/scale), x_center_for_x_coord_legend-75, 80));
        add(lbl("-"+(int)Math.ceil(200*3/scale), x_center_for_x_coord_legend-36, 80));
        add(lbl("0",    x_center_for_x_coord_legend,    80));
        add(lbl("+"+(int)Math.ceil(200*3/scale), x_center_for_x_coord_legend+36, 80));
        add(lbl("+"+(int)Math.ceil(400*3/scale), x_center_for_x_coord_legend+75, 80));

        add(lbl("Colour values", -111, 297));

        int big_cell_y_lvl = 307;
        int big_cell_step = 1;
        int base = -111-big_cell_step;
        for(int i = 0; i <= 200; i++){
            add(bigger_cell(base += big_cell_step, big_cell_y_lvl, count2colour(i*5)));
        }

        int y_level_for_colour_legend = 316;
        add(lbl("1",     -111, y_level_for_colour_legend));
        add(lbl("500",   -11,  y_level_for_colour_legend));
        add(lbl("1000+", 80,   y_level_for_colour_legend));
    }

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
        return hor_line(x, y, 0x55000000);
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
        return ver_line(x, y, 0x55000000);
    }

    private PaneBox ver_line(int x, int y, int colour){
        int height = 206;
        PaneBox box = basicBox(x, y);
        box.setColor(colour);
        box.setHeight(height);
        box.setZIndex(3);
        return box;
    }

    private PaneBox cell(int x_offset, int y_offset, int colour, Object label){
        int box_size = scale;
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
        box.setWidth(1);
        box.setHeight(5);
        box.setZIndex(1);
        return box;
    }

    public int count2colour(int n){
        float index = (float)n;
        if (index >= 1000f) index = 1000f;
//        index = index-40f/360f;
        index = (index*(300f/1000f))/360f;
        return Color.HSBtoRGB(index, 0.8f, 1.0f);
    }

//    private int count2colour(int n){
//             if (n >= 1   && n <= 30)        return 0x20000000;
//        else if (n >= 31  && n <= 100)       return 0x50000000;
//        else if (n >= 101 && n <= 500)       return 0x90000000;
//        else if (n >= 501 && n <= 1000)      return 0xb0000000;
//        else if (n >  1000)                  return 0xe0000000;
//        else                                 return 0x00ffffff;
//    }

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
