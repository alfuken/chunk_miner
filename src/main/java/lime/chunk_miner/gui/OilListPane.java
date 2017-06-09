package lime.chunk_miner.gui;

import gminers.glasspane.GlassPane;
import gminers.glasspane.component.PaneScrollPanel;
import gminers.glasspane.component.button.PaneButton;
import gminers.glasspane.component.text.PaneLabel;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;

import static lime.chunk_miner.ChunkMinerHelpers.getScanDataCoordsByName;

public class OilListPane extends GlassPane {
    private Map<String, HashSet<String>> prepare_data(Map<String, String> data){
        Map<String, HashSet<String>> res = new HashMap<String, HashSet<String>>();

        for(String coords : data.keySet()){
            try {
                String[] pair = data.get(coords).split(" x ");
                HashSet<String> stored_amounts = res.get(pair[1]);
                if (stored_amounts == null) stored_amounts = new HashSet<String>();
                stored_amounts.add(pair[0]);
                res.put(pair[1], stored_amounts);
            } catch(ArrayIndexOutOfBoundsException e) {}
        }

        return res;
    }

    public OilListPane(final EntityPlayer player, final Map<String, String> data) {
//        System.out.println(data.toString());
        setRevertAllowed(true);
        setName("OilListPane");
        setShadowbox(null);

        add(GuiHelpers.book_background());
        add(GuiHelpers.back_button());

        PaneScrollPanel scroll_panel = GuiHelpers.scroll_panel("Scan registry");
        Map<String, HashSet<String>> prepared_data = prepare_data(data);

        int line = 0;
        for(final String name : new TreeSet<String>(prepared_data.keySet()) ){
            if (name == null) continue;

            PaneLabel name_label = PaneLabel.createTitleLabel(name);
//            name_label.setAutoPositionX(true);
//            name_label.setRelativeX(0.5D);
//            name_label.setRelativeXOffset(-120);
            name_label.setX(30);
            name_label.setY(50+(15*line));
            name_label.setColor(0x333333);
            name_label.setShadow(false);
            scroll_panel.add(name_label);
            line++;

//            List<Integer> ints = new ArrayList<Integer>();
            int[] ints = new int[prepared_data.get(name).size()];
            int i = 0;
            for(String s : prepared_data.get(name)){
                if (Integer.parseInt(s)> 0){
                    ints[i] = (Integer.parseInt(s));
                    i++;
                }
            }

            Arrays.sort(ints);

            for(final int c : ints){
                ClickablePaneLabel btn = new ClickablePaneLabel(c+"");
                btn.setWidth(180);
                btn.setHeight(10);
                btn.setX(40);
                btn.setY(50+(15*line));
                btn.setColor(0x111111);
                btn.setShadow(false);
                btn.registerActivationListener(new Runnable() {
                    @Override
                    public void run() {
                        new MapPane(player, name, getScanDataCoordsByName(data, c+" x "+name)).show();
                    }
                });
                scroll_panel.add(btn);
                line++;
            }

        }

        add(scroll_panel);

    }
}
