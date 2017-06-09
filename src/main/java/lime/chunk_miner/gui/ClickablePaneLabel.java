package lime.chunk_miner.gui;

import com.google.common.collect.Lists;
import gminers.glasspane.HorzAlignment;
import gminers.glasspane.VertAlignment;
import gminers.glasspane.component.Focusable;
import gminers.glasspane.component.PaneComponent;
import gminers.glasspane.component.text.PaneLabel;
import gminers.glasspane.event.ComponentActivateEvent;
import gminers.glasspane.event.FocusGainedEvent;
import gminers.glasspane.listener.PaneEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;
import java.util.List;

public class ClickablePaneLabel extends PaneLabel implements Focusable {

    protected boolean enabled;
    protected int buttonColor;
    protected int disabledColor;
    protected int hoveredColor;
    private List<Runnable> activationListeners;

    public ClickablePaneLabel() {
        this("Button");
    }

    public ClickablePaneLabel(String text) {
        this.enabled = true;
        this.buttonColor = 16777215;
        this.disabledColor = 10526880;
        this.hoveredColor = 16777120;
        this.activationListeners = Lists.newArrayList();
        this.text = text;
        this.color = 14737632;
        this.alignmentX = HorzAlignment.LEFT;
        this.alignmentY = VertAlignment.MIDDLE;
        this.width = 200;
        this.height = 20;
        if (this.text == null) this.text = "<this.text>";
    }

    protected void doTick() {
        super.doTick();
        if(!this.enabled && this.getParent() != null && this.getParent().getFocusedComponent() == this) {
            this.getParent().setFocusedComponent((PaneComponent)null);
        }

    }

    protected void doRender(int mouseX, int mouseY, float partialTicks) {
        boolean hover = Mouse.isInsideWindow() && this.withinBounds(mouseX, mouseY);

        Minecraft.getMinecraft().renderEngine.bindTexture(RESOURCE);
        int r = this.buttonColor >> 16 & 255;
        int g = this.buttonColor >> 8 & 255;
        int b = this.buttonColor & 255;

        GL11.glColor3f((float)r / 255.0F, (float)g / 255.0F, (float)b / 255.0F);
        GL11.glTranslatef(0.0F, 0.0F, 0.001F);

        int trueColor = this.color;
        if(!this.enabled) {
            this.color = this.disabledColor;
        } else if(hover) {
            this.color = this.hoveredColor;
        }

        GL11.glTranslatef(0.0F, 0.0F, 0.001F);
        if(this.alignmentX == HorzAlignment.RIGHT && !this.text.endsWith(" ")) {
            this.text = this.text + " ";
        } else if(this.alignmentX == HorzAlignment.LEFT && !this.text.startsWith(" ")) {
            this.text = " " + this.text;
        }

        this.labelRender(mouseX, mouseY, partialTicks);
        this.color = trueColor;
    }

    public String getText() {
        return this.alignmentX == HorzAlignment.RIGHT && this.text.endsWith(" ")?this.text.substring(0, this.text.length() - 1):(this.alignmentX == HorzAlignment.LEFT && this.text.startsWith(" ")?this.text.substring(1):this.text);
    }

    protected void labelRender(int mouseX, int mouseY, float partialTicks) {
        super.doRender(mouseX, mouseY, partialTicks);
    }

    public void registerActivationListener(Runnable r) {
        Validate.notNull(r, "Runnable cannot be null", new Object[0]);
        this.activationListeners.add(r);
    }

    public void unregisterActivationListener(Runnable r) {
        this.activationListeners.remove(r);
    }

    @PaneEventHandler
    public void onActivate(ComponentActivateEvent e) {
        if(this.enabled) {
            Iterator i$ = this.activationListeners.iterator();

            while(i$.hasNext()) {
                Runnable r = (Runnable)i$.next();
                r.run();
            }

            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
        } else {
            e.consume();
        }

    }

    @PaneEventHandler
    public void onFocus(FocusGainedEvent e) {
        if(!this.enabled) {
            e.consume();
        }

    }

    public String toString() {
        return "PaneButton(enabled=" + this.isEnabled() + ", buttonColor=" + this.getButtonColor() + ", disabledColor=" + this.getDisabledColor() + ", hoveredColor=" + this.getHoveredColor() + ", activationListeners=" + this.getActivationListeners() + ")";
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public int getButtonColor() {
        return this.buttonColor;
    }

    public int getDisabledColor() {
        return this.disabledColor;
    }

    public int getHoveredColor() {
        return this.hoveredColor;
    }

    public List<Runnable> getActivationListeners() {
        return this.activationListeners;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setButtonColor(int buttonColor) {
        this.buttonColor = buttonColor;
    }

    public void setDisabledColor(int disabledColor) {
        this.disabledColor = disabledColor;
    }

    public void setHoveredColor(int hoveredColor) {
        this.hoveredColor = hoveredColor;
    }

    public void setActivationListeners(List<Runnable> activationListeners) {
        this.activationListeners = activationListeners;
    }
}
