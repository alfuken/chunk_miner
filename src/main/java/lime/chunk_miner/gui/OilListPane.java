package lime.chunk_miner.gui;

import gminers.glasspane.GlassPane;
import gminers.glasspane.component.PaneScrollPanel;
import lime.chunk_miner.ScanDB;

public class OilListPane extends GlassPane {
    public OilListPane() {
        setRevertAllowed(true);
        setName("OilListPane");
        setShadowbox(null);

        add(GuiHelpers.book_background());
        add(GuiHelpers.back_button());

        PaneScrollPanel scroll_panel = GuiHelpers.scroll_panel("Scan registry");

        int i = 0;
        for(final String name : ScanDB.get_oil_names()){
            if (name == null || name.equals("")) continue;
            ClickablePaneLabel btn = GuiHelpers.clickable_label(name, i++, new Runnable() {
                @Override
                public void run() {
                    new MapPane(name).show();
                }
            });
            scroll_panel.add(btn);
        }

        ClickablePaneLabel btn = GuiHelpers.clickable_label("All Oil and Gas", i++, new Runnable() {
            @Override
            public void run() {
            new MapPane("All Oil and Gas").show();
            }
        });

        scroll_panel.add(btn);

        add(scroll_panel);

    }
}
