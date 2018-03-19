package lime.chunk_miner.gui;

import gminers.glasspane.GlassPane;
import gminers.glasspane.component.PaneScrollPanel;
import gminers.glasspane.component.button.PaneButton;
import gminers.glasspane.component.button.PaneCheckBox;
import gminers.glasspane.component.numeric.PaneSlider;
import gminers.glasspane.event.StateChangedEvent;
import gminers.glasspane.listener.PaneEventHandler;
import lime.chunk_miner.ScanDB;

import java.util.ArrayList;

public class OilListPane extends GlassPane {
    public ArrayList<PaneCheckBox> checkboxes = new ArrayList<PaneCheckBox>();
    public static ArrayList<String> selected = new ArrayList<String>();
    public static OilListPane instance;

    public OilListPane()
    {
        if (selected.isEmpty()) selected.addAll(ScanDB.get_oil_names());
        instance = this;
        setRevertAllowed(true);
        setName("OilListPane");
        setShadowbox(null);

        add(GuiHelpers.book_background());
        add(GuiHelpers.back_button());

        PaneScrollPanel scroll_panel = GuiHelpers.scroll_panel("Scan registry");

        int i = 0;
        for(final String name : ScanDB.get_oil_names())
        {
            if (name == null || name.equals("")) continue;

            final PaneCheckBox checkBox = new PaneCheckBox(name);
            checkBox.setWidth(180);
            checkBox.setHeight(10);
            checkBox.setX(30);
            checkBox.setY(35+(13*i++));
            checkBox.setColor(0xFF111111);
            checkBox.setShadow(false);
            checkBox.setSelected(selected.contains(name));

            checkboxes.add(checkBox);

            scroll_panel.add(checkBox);
        }


        final PaneSlider scaleSlider = new PaneSlider();
        scaleSlider.setText("Zoom level: "+MapPane.scale+"/6");
        scaleSlider.registerListeners(new Object() {
            @PaneEventHandler
            public void onStateChanged(StateChangedEvent e) {
                scaleSlider.setText("Zoom level: " + scaleSlider.getValue() + "/6");
                MapPane.setScale(scaleSlider.getValue());
            }
        });
        scaleSlider.setValue(MapPane.scale);
        scaleSlider.setWidth(180);
        scaleSlider.setMaximum(6);
        scaleSlider.setX(30);
        scaleSlider.setY(35+(13*i++)+10);
        scroll_panel.add(scaleSlider);


        final PaneSlider minSlider = new PaneSlider();
        minSlider.setText("Minimal richness: "+MapPane.min);
        minSlider.registerListeners(new Object() {
            @PaneEventHandler
            public void onStateChanged(StateChangedEvent e) {
                minSlider.setText("Minimal richness: " + minSlider.getValue()*10);
                MapPane.setMin(minSlider.getValue()*10);
            }
        });
        minSlider.setValue(MapPane.min);
        minSlider.setWidth(180);
        minSlider.setMaximum(100);
        minSlider.setX(30);
        minSlider.setY(35+(13*i++)+20);
        scroll_panel.add(minSlider);


        final PaneButton show = new PaneButton("Show selected on map");
        show.setWidth(180);
        show.setHeight(20);
        show.setX(30);
        show.setY(35+(13*i++)+30);
        show.setColor(0xFF111111);
        show.setShadow(false);
        show.registerActivationListener(new Runnable() {
            public void run() {
                ArrayList<String> names = new ArrayList<String>();
                for (PaneCheckBox cb : instance.checkboxes)
                {
                    if (cb.isSelected())
                    {
                        names.add(cb.getText());
                        selected.add(cb.getText());
                    }
                    else
                    {
                        selected.remove(cb.getText());
                    }
                }
                if (selected.isEmpty()){
                    names.addAll(ScanDB.get_oil_names());
                    selected.addAll(names);
                }
                new MapPane(names).show();
            }
        });
        scroll_panel.add(show);

        add(scroll_panel);

    }
}
