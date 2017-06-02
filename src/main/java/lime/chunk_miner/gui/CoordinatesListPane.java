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
    private List<String> prepare_coords(EntityPlayer player, List<String> coords){
        Map<Double, String> new_coords = new HashMap<Double, String>();
        for(String e : coords){
            String[] pair = e.split(":");
            double d = player.getDistance(Integer.parseInt(pair[0]), player.posY, Integer.parseInt(pair[1]));
            new_coords.put(d, e);
        }

        List<String> new_coords_list = new ArrayList<String>();
        for(Double key : new TreeSet<Double>(new_coords.keySet())){
            new_coords_list.add(key.intValue()+"   "+new_coords.get(key));
        }

        return new_coords_list;
    }

    public CoordinatesListPane(EntityPlayer player, String name, List<String> data){
        setRevertAllowed(true);
        setName("CoordinatesListPane");
        setShadowbox(null);

        PaneImage image = new PaneImage(new ResourceLocation("textures/gui/my_book.png"));
        image.setZIndex(-1);
        image.setAutoPositionX(true);
        image.setRelativeX(0.5D);
        image.setRelativeXOffset(-150);
        image.setY(5);
        image.setWidth(297);
        image.setHeight(364);
        add(image);

        final PaneButton back = new PaneButton("<");
        back.setAutoPositionX(true);
        back.setRelativeX(0.5D);
        back.setRelativeXOffset(-115);
        back.setWidth(20);
        back.setHeight(20);
        back.setY(40);
        back.registerActivationListener(new Runnable() {
            public void run() {
                back.getGlassPane().revert();
            }
        });
        add(back);

        PaneScrollPanel scroll_panel = new PaneScrollPanel();
        PaneLabel title_label = PaneLabel.createTitleLabel(name+"\nDistance & Coordinates");
        title_label.setY(15);
        title_label.setColor(0x444444);
        title_label.setShadow(false);

        scroll_panel.add(title_label);

        scroll_panel.setAutoPositionX(true);
        scroll_panel.setRelativeX(0.5D);
        scroll_panel.setRelativeXOffset(-135);
        scroll_panel.setWidth(280);
        scroll_panel.setHeight(300);
        scroll_panel.setY(30);
        scroll_panel.setShadowed(false);

        int i = 0;
        for(String e : prepare_coords(player, data)){
            PaneLabel label = new PaneLabel(e);
            label.setX(43);
            label.setY(45+(13*i));
            label.setAlignmentX(HorzAlignment.MIDDLE);
            label.setAlignmentY(VertAlignment.MIDDLE);
            label.setColor(0x222222);
            label.setShadow(false);

            scroll_panel.add(label);
            i++;
        }

        add(scroll_panel);

    }
}
