package lime.chunk_miner.gui;

import gminers.glasspane.GlassPane;
import gminers.glasspane.HorzAlignment;
import gminers.glasspane.VertAlignment;
import gminers.glasspane.component.PaneScrollPanel;
import gminers.glasspane.component.button.PaneButton;
import gminers.glasspane.component.text.PaneLabel;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;

public class CoordinatesListPane extends GlassPane {
    private List<String> prepare_coords(EntityPlayer player, List<String> coords){
        Map<Double, String> new_coords = new HashMap<Double, String>();
        for(String e : coords){
            String[] pair = e.split(":");
            double d = player.getDistance(Integer.parseInt(pair[0]), player.posY, Integer.parseInt(pair[1]));
            new_coords.put(d, e);
        }

        List<String> new_coords_list = new ArrayList<String>();
        for(Double key : new TreeSet<Double>(new_coords.keySet())){
            new_coords_list.add(key.intValue()+" - "+new_coords.get(key));
        }

        return new_coords_list;
    }

    public CoordinatesListPane(EntityPlayer player, List<String> data){
        setRevertAllowed(true);
        setName("CoordinatesListPane");
        setShadowbox(null);

        final PaneButton back = new PaneButton("< Back");
//        back.setAutoPositionX(true);
//        back.setRelativeX(0.5D);
//        back.setRelativeXOffset(-90);
        back.setWidth(180);
        back.setHeight(20);
        back.setY(5);
        back.registerActivationListener(new Runnable() {
            public void run() {
                back.getGlassPane().revert();
            }
        });
        add(back);

        PaneScrollPanel scroll_panel = new PaneScrollPanel();
        PaneLabel title_label = PaneLabel.createTitleLabel("Distance - Coordinates");
        title_label.setY(10);
        scroll_panel.add(title_label);

//        scroll_panel.setAutoPositionX(true);
//        scroll_panel.setRelativeX(0.5D);
//        scroll_panel.setRelativeXOffset(-120);

        scroll_panel.setAutoResizeHeight(true);
        scroll_panel.setRelativeHeightOffset(-60);
        scroll_panel.setWidth(240);
        scroll_panel.setY(30);

        int i = 0;
        for(String e : prepare_coords(player, data)){
            PaneLabel label = new PaneLabel(e);
            label.setX(30);
            label.setY(30+(12*i));
            label.setAlignmentX(HorzAlignment.MIDDLE);
            label.setAlignmentY(VertAlignment.MIDDLE);
            i++;
            scroll_panel.add(label);
        }

        add(scroll_panel);

    }
}
