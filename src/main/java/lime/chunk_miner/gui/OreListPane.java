package lime.chunk_miner.gui;

import cpw.mods.fml.common.Loader;
import gminers.glasspane.GlassPane;
import gminers.glasspane.component.PaneScrollPanel;
import gminers.glasspane.component.button.PaneButton;
import lime.chunk_miner.ScanDB;
import net.minecraft.entity.player.EntityPlayer;


public class OreListPane extends GlassPane {
    public OreListPane(final EntityPlayer p){
        setName("OreListPane");
        setShadowbox(null);

        add(GuiHelpers.book_background());
        add(GuiHelpers.close_button());

        if (Loader.isModLoaded("gregtech")) {
            PaneButton oil = new PaneButton("Oil");
            oil.setWidth(25);
            oil.setHeight(15);
            oil.setAutoPositionX(true);
            oil.setRelativeX(0.5D);
            oil.setRelativeXOffset(100);
            oil.setY(15);
            oil.registerActivationListener(new Runnable() {
                @Override
                public void run() {
                    new OilListPane(p).show();
                }
            });
            add(oil);
        }

        PaneScrollPanel scroll_panel = GuiHelpers.scroll_panel("Scan registry");

            int i = 0;
            for(final String name : ScanDB.p(p).get_ore_names()){
                if (name == null || name.equals("")) continue;
                ClickablePaneLabel btn = GuiHelpers.clickable_label(name, i++, new Runnable() {
                    @Override
                    public void run() {
                        new MapPane(p, name).show();
                    }
                });
                scroll_panel.add(btn);
            }

        add(scroll_panel);

    }
}
