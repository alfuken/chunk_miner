package lime.chunk_miner.gui;

import gminers.glasspane.GlassPane;
import gminers.glasspane.component.PaneImage;
import gminers.glasspane.component.PaneScrollPanel;
import gminers.glasspane.component.button.PaneButton;
import gminers.glasspane.component.text.PaneLabel;
import lime.chunk_miner.ChunkMiner;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class ScanResultsPane extends GlassPane {
    public ScanResultsPane(final EntityPlayer player, final Map<String, List<String>> data){
        setName("ScanResultsPane");
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

        PaneButton close = new PaneButton("X");
        close.setWidth(15);
        close.setHeight(15);
        close.setAutoPositionX(true);
        close.setRelativeX(0.5D);
        close.setRelativeXOffset(125);
        close.setY(15);
        close.registerActivationListener(new Runnable() {
            @Override
            public void run() {
                Minecraft.getMinecraft().displayGuiScreen(null);
            }
        });
        add(close);

        PaneScrollPanel scroll_panel = new PaneScrollPanel();
        PaneLabel title_label = PaneLabel.createTitleLabel("Scan registry");
        title_label.setY(15);
        title_label.setColor(0x333333);
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
        for(final String name : new TreeSet<String>(data.keySet())){
            ClickablePaneLabel btn = new ClickablePaneLabel(name);
            btn.setWidth(180);
            btn.setHeight(10);
            btn.setX(30);
            btn.setY(35+(13*i));
            btn.setColor(0x111111);
            btn.setShadow(false);
            btn.registerActivationListener(new Runnable() {
                @Override
                public void run() {
                    new CoordinatesListPane(player, name, data.get(name)).show();
                }
            });
            scroll_panel.add(btn);
            i++;
        }

        add(scroll_panel);
    }
}
