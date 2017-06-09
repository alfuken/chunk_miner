package lime.chunk_miner.gui;

import gminers.glasspane.HorzAlignment;
import gminers.glasspane.VertAlignment;
import gminers.glasspane.component.PaneImage;
import gminers.glasspane.component.PaneScrollPanel;
import gminers.glasspane.component.button.PaneButton;
import gminers.glasspane.component.text.PaneLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class GuiHelpers {
    public static PaneButton close_button(){
        PaneButton close = new PaneButton("x");
        close.setWidth(15);
        close.setHeight(15);
        close.setAutoPositionX(true);
        close.setRelativeX(0.5D);
        close.setRelativeXOffset(125);
        close.setY(15);
        close.setAlignmentY(VertAlignment.TOP);
        close.registerActivationListener(new Runnable() {
            @Override
            public void run() {
                Minecraft.getMinecraft().displayGuiScreen(null);
            }
        });
        return close;
    }

    public static PaneButton back_button(){
        final PaneButton back = new PaneButton("<");
        back.setAutoPositionX(true);
        back.setRelativeX(0.5D);
        back.setRelativeXOffset(-115);
        back.setWidth(20);
        back.setHeight(20);
        back.setY(43);
        back.registerActivationListener(new Runnable() {
            public void run() {
                back.getGlassPane().revert();
            }
        });
        return back;
    }

    public static PaneImage book_background(){
        return book_background("my_book");
    }

    public static PaneImage book_background(String name){
        PaneImage image = new PaneImage(new ResourceLocation("textures/gui/"+name+".png"));
        image.setZIndex(-2);
        image.setAutoPositionX(true);
        image.setRelativeX(0.5D);
        image.setRelativeXOffset(-150);
        image.setY(5);
        image.setWidth(297);
        image.setHeight(364);
        return image;
    }

    public static PaneLabel scroll_panel_title(String text){
        PaneLabel title_label = PaneLabel.createTitleLabel(text);
        title_label.setY(15);
        title_label.setColor(0x333333);
        title_label.setShadow(false);
        return title_label;
    }

    public static PaneScrollPanel scroll_panel(String text){
        PaneScrollPanel scroll_panel = new PaneScrollPanel();
        scroll_panel.add(GuiHelpers.scroll_panel_title(text));
        scroll_panel.setAutoPositionX(true);
        scroll_panel.setRelativeX(0.5D);
        scroll_panel.setRelativeXOffset(-135);
        scroll_panel.setWidth(280);
        scroll_panel.setHeight(300);
        scroll_panel.setY(30);
        scroll_panel.setShadowed(false);
        return scroll_panel;
    }

    public static ClickablePaneLabel clickable_label(String name, int i, Runnable callback) {
        ClickablePaneLabel btn = new ClickablePaneLabel(name);
        btn.setWidth(180);
        btn.setHeight(10);
        btn.setX(30);
        btn.setY(35+(13*i));
        btn.setColor(0x111111);
        btn.setShadow(false);
        btn.registerActivationListener(callback);
        return btn;
    }

    public static PaneLabel label(String text, int i){
        PaneLabel label = new PaneLabel(text);
        label.setX(43);
        label.setY(45+(13*i));
        label.setAlignmentX(HorzAlignment.MIDDLE);
        label.setAlignmentY(VertAlignment.MIDDLE);
        label.setColor(0x222222);
        label.setShadow(false);
        return label;
    }
}
