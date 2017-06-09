package lime.chunk_miner.gui;

import cpw.mods.fml.common.Loader;
import gminers.glasspane.GlassPane;
import gminers.glasspane.component.PaneScrollPanel;
import gminers.glasspane.component.button.PaneButton;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Map;

import static lime.chunk_miner.ChunkMinerHelpers.getScanDataCoordsByName;
import static lime.chunk_miner.ChunkMinerHelpers.getScanDataNames;

public class OreListPane extends GlassPane {
    public OreListPane(final EntityPlayer player, final Map<String, String> data, final Map<String, String> oil_data){
        setName("OreListPane");
        setShadowbox(null);

        add(GuiHelpers.book_background());
        add(GuiHelpers.close_button());

        if (Loader.isModLoaded("gregtech")) {
            PaneButton oil = new PaneButton("oil");
            oil.setWidth(25);
            oil.setHeight(15);
            oil.setAutoPositionX(true);
            oil.setRelativeX(0.5D);
            oil.setRelativeXOffset(100);
            oil.setY(15);
            oil.registerActivationListener(new Runnable() {
                @Override
                public void run() {
                    new OilListPane(player, oil_data).show();
                }
            });
            add(oil);
        }

        PaneScrollPanel scroll_panel = GuiHelpers.scroll_panel("Scan registry");

            int i = 0;
            for(final String name : getScanDataNames(data)){
                if (name == null) continue;
                ClickablePaneLabel btn = GuiHelpers.clickable_label(name, i, new Runnable() {
                    @Override
                    public void run() {
                        new MapPane(player, name, getScanDataCoordsByName(data, name)).show();
//                        new CoordinatesListPane(player, name, getScanDataCoordsByName(data, name)).show();
                    }
                });
                scroll_panel.add(btn);
                i++;
            }

        add(scroll_panel);

    }
}
