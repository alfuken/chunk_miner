package lime.chunk_miner.gui;

import gminers.glasspane.GlassPane;
import gminers.glasspane.component.PaneComponent;
import gminers.glasspane.component.PanePanel;
import gminers.glasspane.component.button.PaneButton;
import gminers.glasspane.component.text.*;
import gminers.glasspane.event.KeyTypedEvent;
import gminers.glasspane.listener.PaneEventHandler;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class ScanResultsPane extends GlassPane {
    String current_filter = "";
    List<String> data = new ArrayList<String>();
    List<PaneButton> buttons = new ArrayList<PaneButton>();

    public List<String> filteredData() {
        if (current_filter == "") return data;
        List<String> new_list = new ArrayList<String>();
        for(String name : data){
            if (name.contains(current_filter)){
                new_list.add(name);
            }
        }
        return new_list;
    }

    void refreshButtons(){
        removeButtons();
        addButtons();
    }

    void addButtons(){
        int i = 0;
        for(String name : filteredData()){
            PaneButton btn = new PaneButton(name);
            btn.setX(40);
            btn.setY(100+(24*i));
            i++;
            buttons.add(btn);
            add(btn);
        }
    }

    void removeButtons(){
        for(PaneButton btn : buttons){
            remove(btn);
            buttons.remove(btn);
        }
    }


    public ScanResultsPane(List<String> data){
        this.data = data;

        addButtons();

        setName("Scan results DB");
        setRevertAllowed(true);
        add(PaneButton.createDoneButton());

        final PaneButton close = new PaneButton("X");
//        done.setAutoResize(true);
        close.setRelativeX(1.0D);
        close.setRelativeXOffset(-30);
        close.setY(30);
        close.setWidth(20);
        close.setHeight(20);
        close.registerListeners(new Runnable() {
            @Override
            public void run() {
                Minecraft.getMinecraft().displayGuiScreen(null);
            }
        });
        add(close);

        PanePanel left_panel = new PanePanel();
        left_panel.setBorderText("Search");
        left_panel.setAutoResize(true);
        left_panel.setX(10);
        left_panel.setY(10);
        left_panel.setRelativeWidth(0.48);
        left_panel.setRelativeHeight(1.0);
        left_panel.setRelativeWidthOffset(-20);
        left_panel.setRelativeHeightOffset(-20);
        add(left_panel);

        PanePanel right_panel = new PanePanel();
        right_panel.setBorderText("Scan results");
        right_panel.setAutoResize(true);
        right_panel.setRelativeX(0.5);
        right_panel.setY(10);
        right_panel.setRelativeWidth(0.48);
        right_panel.setRelativeHeight(1.0);
        right_panel.setRelativeWidthOffset(-20);
        right_panel.setRelativeHeightOffset(-20);
        add(left_panel);

        final PaneLabel label = new PaneLabel(current_filter);
        label.setX(40);
        label.setY(60);

        PaneTextField search_field = new PaneTextField();
        search_field.setBlankText("Enter text to search");
        search_field.setX(30);
        search_field.setY(30);

        search_field.registerListeners(new Object() {
            @PaneEventHandler
            public void search_field_key_typed(KeyTypedEvent e) {
                String txt = ((PaneTextField)e.getSource()).getText();
                if (txt != current_filter){
                    label.setText(txt);
                    current_filter = txt;
                    refreshButtons();
                }
            }
        });
        add(search_field, label);
    }
}
