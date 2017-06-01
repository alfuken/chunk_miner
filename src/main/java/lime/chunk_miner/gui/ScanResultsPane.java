package lime.chunk_miner.gui;

import gminers.glasspane.GlassPane;
import gminers.glasspane.HorzAlignment;
import gminers.glasspane.component.PaneScrollPanel;
import gminers.glasspane.component.button.PaneButton;
import gminers.glasspane.component.text.PaneLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;

public class ScanResultsPane extends GlassPane {
    public ScanResultsPane(final EntityPlayer player, final Map<String, List<String>> data){
        setName("ScanResultsPane");
        setShadowbox(null);

        PaneButton close = new PaneButton("Close");
//        close.setAutoPositionX(true);
//        close.setRelativeX(0.5D);
//        close.setRelativeXOffset(-90);
        close.setY(5);
        close.setWidth(180);
        close.registerActivationListener(new Runnable() {
            @Override
            public void run() {
                Minecraft.getMinecraft().displayGuiScreen(null);
            }
        });
        add(close);

        PaneScrollPanel scroll_panel = new PaneScrollPanel();
        PaneLabel title_label = PaneLabel.createTitleLabel("Scan registry");
        title_label.setY(10);
        scroll_panel.add(title_label);

//        scroll_panel.setAutoPositionX(true);
//        scroll_panel.setRelativeX(0.5D);
//        scroll_panel.setRelativeXOffset(-120);

        scroll_panel.setAutoResizeHeight(true);
        scroll_panel.setRelativeHeightOffset(-60);
        scroll_panel.setWidth(240);
        scroll_panel.setY(30);
//        scroll_panel.setShadowed(false);

        int i = 0;
        for(final String name : new TreeSet<String>(data.keySet())){
            PaneButton btn = new PaneButton(name);
            btn.setWidth(180);
            btn.setX(30);
            btn.setY(25+(23*i));
            btn.registerActivationListener(new Runnable() {
                @Override
                public void run() {
                    new CoordinatesListPane(player, data.get(name)).show();
                }
            });
            scroll_panel.add(btn);
            i++;
        }

        add(scroll_panel);
    }
}
