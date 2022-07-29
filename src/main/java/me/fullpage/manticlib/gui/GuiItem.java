package me.fullpage.manticlib.gui;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.fullpage.manticlib.wrappers.SoundEffect;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode
@ToString
public class GuiItem {

    private final ItemStack item;
    private @Nullable Gui.ClickAction action;
    private @Nullable Gui.CloseAction closeAction;
    private @Nullable SoundEffect soundEffect;

    public GuiItem(ItemStack item, @Nullable Gui.ClickAction click) {
        this.item = item;
        this.action = click;
        this.soundEffect = null;
    }

    public GuiItem(ItemStack item) {
        this.item = item;
        this.action = action -> true;
        this.soundEffect = null;
    }

    public ItemStack getItem() {
        return this.item;
    }

    @Nullable
    public Gui.ClickAction getAction() {
        return action;
    }

    @Nullable
    public Gui.CloseAction getCloseAction() {
        return closeAction;
    }

    public void setCloseAction(@Nullable Gui.CloseAction closeAction) {
        this.closeAction = closeAction;
    }

    public void setAction(@Nullable Gui.ClickAction action) {
        this.action = action;
    }

    public GuiItem closeAction(@Nullable Gui.CloseAction closeAction) {
        this.closeAction = closeAction;
        return this;
    }

    public GuiItem action(@Nullable Gui.ClickAction action) {
        this.action = action;
        return this;
    }

    public KeyGuiItem asKeyGuiItem(char key) {
        if (this instanceof KeyGuiItem) {
            return (KeyGuiItem) this;
        }
        final KeyGuiItem keyGUIItem = new KeyGuiItem(this.item, this.action, key);
        keyGUIItem.soundEffect(soundEffect);
        keyGUIItem.setCloseAction(closeAction);
        return keyGUIItem;
    }

    public GuiItem soundEffect(@Nullable SoundEffect soundEffect) {
        this.soundEffect = soundEffect;
        return this;
    }

    @Nullable
    public SoundEffect getSoundEffect() {
        return soundEffect;
    }
}