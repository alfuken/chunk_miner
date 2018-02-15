package lime.chunk_miner.gui;

import gminers.glasspane.GlassPane;
import gminers.glasspane.component.PaneScrollPanel;
import gminers.glasspane.component.text.PaneLabel;
import lime.chunk_miner.ScanDB;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;

public class OilListPane extends GlassPane {
    public OilListPane(final EntityPlayer p) {
        setRevertAllowed(true);
        setName("OilListPane");
        setShadowbox(null);

        add(GuiHelpers.book_background());
        add(GuiHelpers.back_button());

        PaneScrollPanel scroll_panel = GuiHelpers.scroll_panel("Scan registry");

        int i = 0;
        for(final String name : ScanDB.p(p).get_oil_names()){
            if (name == null || Objects.equals(name, "")) continue;
            ClickablePaneLabel btn = GuiHelpers.clickable_label(name, i++, new Runnable() {
                @Override
                public void run() {
                    new MapPane(p, name).show();
                }
            });
            scroll_panel.add(btn);
        }


    }
}
