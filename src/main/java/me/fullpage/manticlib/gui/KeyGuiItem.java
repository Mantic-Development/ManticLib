package me.fullpage.manticlib.gui;

import lombok.EqualsAndHashCode;
import me.fullpage.manticlib.wrappers.SoundEffect;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode(callSuper = false)
public class KeyGuiItem extends GuiItem {

    private char key;

    public KeyGuiItem(ItemStack item, @Nullable Gui.ClickAction click, char key) {
        super(item, click);
        this.key = key;
    }

    public KeyGuiItem(ItemStack item, char key) {
        super(item, null);
        this.key = key;
    }

    public char getKey() {
        return key;
    }

    public void setKey(char key) {
        this.key = key;
    }

    @Override
    public KeyGuiItem action(@Nullable Gui.ClickAction action) {
        return (KeyGuiItem) super.action(action);
    }

    @Override
    public KeyGuiItem closeAction(@Nullable Gui.CloseAction closeAction) {
        return (KeyGuiItem) super.closeAction(closeAction);
    }

    @Override
    public KeyGuiItem soundEffect(@Nullable SoundEffect soundEffect) {
        return (KeyGuiItem) super.soundEffect(soundEffect);
    }
}
