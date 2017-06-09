package lime.chunk_miner.gui;

import gminers.glasspane.GlassPane;
import gminers.glasspane.HorzAlignment;
import gminers.glasspane.VertAlignment;
import gminers.glasspane.component.PaneImage;
import gminers.glasspane.component.PaneScrollPanel;
import gminers.glasspane.component.button.PaneButton;
import gminers.glasspane.component.text.PaneLabel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class CoordinatesListPane extends GlassPane {
    private List<String> prepare_coords(EntityPlayer player, String[] coords){
        Map<Double, String> new_coords = new HashMap<Double, String>();
        for(String e : coords){
            String[] pair = e.split(":");
            double d = player.getDistance(Integer.parseInt(pair[0])*16, player.posY, Integer.parseInt(pair[1])*16);
            new_coords.put(d, Integer.parseInt(pair[0])*16+":"+Integer.parseInt(pair[1])*16+" ("+e+")");
        }

        List<String> new_coords_list = new ArrayList<String>();
        for(Double key : new TreeSet<Double>(new_coords.keySet())){
            new_coords_list.add(key.intValue()+"   "+new_coords.get(key));
        }

        return new_coords_list;
    }

    public CoordinatesListPane(final EntityPlayer player, final String name, final String[] data){
        setRevertAllowed(true);
        setName("CoordinatesListPane");
        setShadowbox(null);

        add(GuiHelpers.book_background());
        add(GuiHelpers.back_button());

        PaneScrollPanel scroll_panel = GuiHelpers.scroll_panel(name+"\nDistance & Coordinates (Chunk)");

            int i = 1;
            for(String e : prepare_coords(player, data)){
                PaneLabel label = GuiHelpers.label(e, i);
                scroll_panel.add(label);
                i++;
            }

        add(scroll_panel);

        PaneButton map = new PaneButton("map");
        map.setWidth(25);
        map.setHeight(15);
        map.setAutoPositionX(true);
        map.setRelativeX(0.5D);
        map.setRelativeXOffset(75);
        map.setY(15);
        map.registerActivationListener(new Runnable() {
            @Override
            public void run() {
                new MapPane(player, name, data).show();
            }
        });
        add(map);
    }
}
