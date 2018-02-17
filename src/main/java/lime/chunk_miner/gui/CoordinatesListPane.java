package lime.chunk_miner.gui;

import gminers.glasspane.GlassPane;
import gminers.glasspane.component.PaneScrollPanel;
import gminers.glasspane.component.button.PaneButton;
import gminers.glasspane.component.text.PaneLabel;
import lime.chunk_miner.ScanDB;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;


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
            for(String e : ScanDB.get(name)){
                PaneLabel label = GuiHelpers.label(e, ++i);
                scroll_panel.add(label);
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
