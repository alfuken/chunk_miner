package lime.chunk_miner.gui;

import gminers.glasspane.GlassPane;
import gminers.glasspane.HorzAlignment;
import gminers.glasspane.component.PaneBox;
import gminers.glasspane.component.button.PaneButton;
import gminers.glasspane.component.text.PaneLabel;
import lime.chunk_miner.ScanDB;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.chunk.Chunk;

import java.util.Map;

public class MapPane extends GlassPane {

    public MapPane(final EntityPlayer p, final String name){
        setRevertAllowed(true);
        setName("MapPane");
        setShadowbox(null);

        add(GuiHelpers.book_background());
        add(GuiHelpers.back_button());

        PaneButton coords_button = new PaneButton("coords");
        coords_button.setWidth(25);
        coords_button.setHeight(15);
        coords_button.setAutoPositionX(true);
        coords_button.setRelativeX(0.5D);
        coords_button.setRelativeXOffset(100);
        coords_button.setY(15);
        coords_button.registerActivationListener(new Runnable() {
            @Override
            public void run() {
                new CoordinatesListPane(p, name).show();
            }
        });
        add(coords_button);


        for (int x = 0; x <= 10; x++) {
            for (int y = 0; y <= 10; y++) {
                if ((x%2==0 && y%2==0) || (x%2==1 && y%2==1)){
                    add(background_tile_dark(x,y));
                } else {
                    add(background_tile_light(x,y));
                }
            }
        }

        Chunk chunk = p.worldObj.getChunkFromBlockCoords((int)p.posX, (int)p.posZ);
        int center_x = chunk.xPosition;
        int center_z = chunk.zPosition;

        add( cell(0, 0, 0x333333, "You"));

        Map<Integer, Map<Integer, Integer>> coords = ScanDB.p(p).get(name, chunk.xPosition, chunk.zPosition, 26);

        for(Map.Entry<Integer, Map<Integer, Integer>> e : coords.entrySet()){
            int x = e.getKey();
            Map<Integer, Integer> zn = e.getValue();
            for(Map.Entry<Integer, Integer> ee : zn.entrySet()){
                int z = ee.getKey();
                int n = ee.getValue();
                int colour;
                String label;
                if (isOil(name)) {
                    colour = oilColour(name);
                    label  = name + ": "+ n;
                } else {
                    colour = count2colour(n);
                    label  = String.valueOf(n);
                }
                add(cell( x-center_x, z-center_z, colour, label));
            }
        }

        PaneLabel label = new PaneLabel(name+"\noccurences map (chunks)");
        label.setAutoResizeWidth(true);
        label.setAlignmentX(HorzAlignment.MIDDLE);
        label.setY(45);
        label.setColor(0x000000);
        label.setShadow(false);
        add(label);
    }

    private PaneBox background_tile_light(int x, int y){
        return background_tile(x, y, 0xE7CFB4);
    }

    private PaneBox background_tile_dark(int x, int y){
        return background_tile(x, y, 0xBE915B);
    }

    private PaneBox background_tile(int x, int y, int colour){
        PaneBox box = new PaneBox(colour);
        box.setAutoPositionX(true);
        box.setRelativeX(0.5D);
        box.setRelativeXOffset((x*20)-115);
        box.setY((y*20)+77);
        box.setWidth(20);
        box.setHeight(20);
        box.setZIndex(-1);
        return box;
    }

    private PaneBox cell(int x_offset, int y_offset, int colour, Object label){
        int box_size = 4;
        PaneBox box = new PaneBox(colour);
        box.setTooltip(String.valueOf(label));
        box.setAutoPositionX(true);
        box.setRelativeX(0.5D);
        box.setRelativeXOffset((x_offset*box_size)-7);
        box.setY((y_offset*box_size)+185);
        box.setWidth(box_size);
        box.setHeight(box_size);
        return box;
    }

    /*
        1-20: красный
        21-50: оранжевый
        51-100: желтый
        101-400: зеленый
        401 и более: ярко-синий
    */
    private int count2colour(int n){
        if (n >= 1 && n <= 20) return 0xe22f02;
        else if (n >= 21 && n <= 50) return 0xff9d00;
        else if (n >= 51 && n <= 100) return 0xffff00;
        else if (n >= 101 && n <= 300) return 0x14d802;
        else if (n >= 301 && n <= 500) return 0x02e6f2;
        else if (n >= 501 && n <= 1000) return 0x0245ff;
        else if (n > 1000) return 0xff01d0;
        else return 0xffffff;
    }

    private int oilColour(String name){
        if (name.equals("Natural Gas")) {
            return 0x80daeb;
        } else if (name.equals("Light Oil")) {
            return 0xffdd2b;
        } else if (name.equals("Heavy Oil")) {
            return 0xc47c0f;
        } else if (name.equals("Raw Oil")) {
            return 0x773e01;
        } else if (name.equals("Oil")) {
            return 0x000000;
        } else {
            return Integer.decode(String.format("0x%06X", (0xFFFFFF & name.hashCode())));
        }
    }

    private boolean isOil(String name){
        return (name.equals("Natural Gas") || name.equals("Light Oil") || name.equals("Heavy Oil") || name.equals("Raw Oil") || name.equals("Oil"));
    }
}
