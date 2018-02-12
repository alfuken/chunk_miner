package lime.chunk_miner.gui;

import gminers.glasspane.GlassPane;
import gminers.glasspane.HorzAlignment;
import gminers.glasspane.component.PaneBox;
import gminers.glasspane.component.button.PaneButton;
import gminers.glasspane.component.text.PaneLabel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.chunk.Chunk;

public class MapPane extends GlassPane {

    public MapPane(final EntityPlayer player, final String name, final String[] coords){
        setRevertAllowed(true);
        setName("MapPane");
        setShadowbox(null);

        int highlight_colour = 0x000000;
        int player_colour = 0x333333;

        if (name.equals("Natural Gas"))    highlight_colour = 0x80daeb;
        else if (name.equals("Light Oil")) highlight_colour = 0xffdd2b;
        else if (name.equals("Heavy Oil")) highlight_colour = 0xc47c0f;
        else if (name.equals("Raw Oil"))   highlight_colour = 0x773e01;
        else if (name.equals("Oil"))       highlight_colour = 0x000000;
        else {
            highlight_colour = Integer.decode(String.format("0x%06X", (0xFFFFFF & name.hashCode())));
        }

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
                new CoordinatesListPane(player, name, coords).show();
            }
        });
        add(coords_button);



        for (int x = 0; x <= 10; x++) {
            for (int y = 0; y <= 10; y++) {
                if ((x%2==0 && y%2==0) || (x%2==1 && y%2==1)){
                    add(bgd(x,y));
                } else {
                    add(bgl(x,y));
                }
            }
        }

        Chunk chunk = player.worldObj.getChunkFromBlockCoords((int)player.posX, (int)player.posZ);
        int px = chunk.xPosition;
        int pz = chunk.zPosition;

        add(b(px, pz, px, pz, 0xfff959, 0));

        for(String e : coords){
            String[] pair = e.split(":");
            int x = Integer.parseInt(pair[0]);
            int z = Integer.parseInt(pair[1]);
//            add(b(x, z, px, pz));
            if ((Math.abs(x-px) <= 26) && (Math.abs(z-pz) <= 26)){
                add(b(x, z, px, pz, highlight_colour, 0));
            }
        }

        PaneLabel label = new PaneLabel(name+"\noccurences map (chunks)");
        label.setAutoResizeWidth(true);
        label.setAlignmentX(HorzAlignment.MIDDLE);
        label.setY(45);
        label.setColor(player_colour);
        label.setShadow(false);
        add(label);
    }

    private PaneBox bgl(int x, int y){
        return bg(x, y, 0xE7CFB4);
    }

    private PaneBox bgd(int x, int y){
        return bg(x, y, 0xBE915B);
    }

    private PaneBox bg(int x, int y, int colour){
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

    private PaneBox b(int x, int y, int px, int py, int colour, int count){
        return b(x,y,px,py,colour,count+"");
    }

    private PaneBox b(int x, int y, int px, int py, int colour){
        return b(x,y,px,py,colour,"");
    }

    private PaneBox b(int x, int y, int px, int py, int colour, String label){
        PaneBox box = new PaneBox(colour);
        if (!label.equals("")) box.setTooltip(label);
        box.setAutoPositionX(true);
        box.setRelativeX(0.5D);
        box.setRelativeXOffset(((x-px)*4)-7);
        box.setY(((y-py)*4)+185);
        box.setWidth(4);
        box.setHeight(4);
        return box;
    }
}
