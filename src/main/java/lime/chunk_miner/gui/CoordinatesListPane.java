package lime.chunk_miner.gui;

import gminers.glasspane.GlassPane;
import gminers.glasspane.component.PaneScrollPanel;
import gminers.glasspane.component.button.PaneButton;
import gminers.glasspane.component.text.PaneLabel;
import lime.chunk_miner.ScanDB;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;


public class CoordinatesListPane extends GlassPane {
    public CoordinatesListPane(final String name){
        final EntityPlayer p = Minecraft.getMinecraft().thePlayer;
        setRevertAllowed(true);
        setName("CoordinatesListPane");
        setShadowbox(null);

        add(GuiHelpers.book_background());
        add(GuiHelpers.back_button());

        PaneScrollPanel scroll_panel = GuiHelpers.scroll_panel(name+" Coordinates");

            int i = 0;
            List<String> names = ScanDB.get(name);
            for(String e : names){
                String[] xzn = StringUtils.split(e, " x ");
                String[] xz  = StringUtils.split(xzn[0], ":");
                int y_offset = i++;

                scroll_panel.add(GuiHelpers.label(xz[0], y_offset));

                PaneLabel label_z = GuiHelpers.label(" : "+xz[1], y_offset);
                label_z.setX(label_z.getX()+50);
                scroll_panel.add(label_z);

                PaneLabel label_n = GuiHelpers.label(" x "+xzn[1], y_offset);
                label_n.setX(label_n.getX()+100);
                scroll_panel.add(label_n);
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
                new MapPane(name).show();
            }
        });
        add(map);
    }
}
