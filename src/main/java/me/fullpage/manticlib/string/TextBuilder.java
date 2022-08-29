package me.fullpage.manticlib.string;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TextBuilder {

    // TODO: under construction from ComponentBuilder

    private TextComponent current;
    private final List<BaseComponent> parts = new ArrayList<>();

    /**
     * Creates a ComponentBuilder from the other given ComponentBuilder to clone
     * it.
     *
     * @param original the original for the new ComponentBuilder.
     */
    public TextBuilder(TextBuilder original) {
        current = new TextComponent(original.current);
        for (BaseComponent baseComponent : original.parts) {
            parts.add(baseComponent.duplicate());
        }
    }

    /**
     * Creates a ComponentBuilder with the given text as the first part.
     *
     * @param text the first text element
     */
    public TextBuilder(String text) {
        current = new TextComponent(text);
    }

    /**
     * Appends the text to the builder and makes it the current target for
     * formatting. The text will have all the formatting from the previous part.
     *
     * @param text the text to append
     * @return this ComponentBuilder for chaining
     */
    public TextBuilder append(String text) {
        return append(text, FormatRetention.ALL);
    }

    /**
     * Appends the text to the builder and makes it the current target for
     * formatting. You can specify the amount of formatting retained.
     *
     * @param text      the text to append
     * @param retention the formatting to retain
     * @return this ComponentBuilder for chaining
     */
    public TextBuilder append(String text, TextBuilder.FormatRetention retention) {
        parts.add(current);

        current = new TextComponent(current);
        current.setText(text);
        retain(retention);

        return this;
    }

    /**
     * Sets the color of the current part.
     *
     * @param color the new color
     * @return this ComponentBuilder for chaining
     */
    public TextBuilder color(ChatColor color) {
        current.setColor(color);
        return this;
    }

    /**
     * Sets whether the current part is bold.
     *
     * @param bold whether this part is bold
     * @return this ComponentBuilder for chaining
     */
    public TextBuilder bold(boolean bold) {
        current.setBold(bold);
        return this;
    }

    /**
     * Sets whether the current part is italic.
     *
     * @param italic whether this part is italic
     * @return this ComponentBuilder for chaining
     */
    public TextBuilder italic(boolean italic) {
        current.setItalic(italic);
        return this;
    }

    /**
     * Sets whether the current part is underlined.
     *
     * @param underlined whether this part is underlined
     * @return this ComponentBuilder for chaining
     */
    public TextBuilder underlined(boolean underlined) {
        current.setUnderlined(underlined);
        return this;
    }

    /**
     * Sets whether the current part is strikethrough.
     *
     * @param strikethrough whether this part is strikethrough
     * @return this ComponentBuilder for chaining
     */
    public TextBuilder strikethrough(boolean strikethrough) {
        current.setStrikethrough(strikethrough);
        return this;
    }

    /**
     * Sets whether the current part is obfuscated.
     *
     * @param obfuscated whether this part is obfuscated
     * @return this ComponentBuilder for chaining
     */
    public TextBuilder obfuscated(boolean obfuscated) {
        current.setObfuscated(obfuscated);
        return this;
    }

    /**
     * Sets the insertion text for the current part.
     *
     * @param insertion the insertion text
     * @return this ComponentBuilder for chaining
     */
    public TextBuilder insertion(String insertion) {
        current.setInsertion(insertion);
        return this;
    }

    /**
     * Sets the click event for the current part.
     *
     * @param clickEvent the click event
     * @return this ComponentBuilder for chaining
     */
    public TextBuilder event(ClickEvent clickEvent) {
        current.setClickEvent(clickEvent);
        return this;
    }

    /**
     * Sets the hover event for the current part.
     *
     * @param hoverEvent the hover event
     * @return this ComponentBuilder for chaining
     */
    public TextBuilder event(HoverEvent hoverEvent) {
        current.setHoverEvent(hoverEvent);
        return this;
    }

    public TextBuilder click(String value, ClickEvent.Action action) {
        return this.event(new ClickEvent(action, value));
    }

    public TextBuilder tooltip(String message) {
        return this.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toBaseComponent(message)));
    }

    public TextBuilder tooltip(String... messages) {
        return this.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toBaseComponent(messages)));
    }

    public static BaseComponent[] toBaseComponent(String message) {
        return TextComponent.fromLegacyText(message);
    }

    public static BaseComponent[] toBaseComponent(String... messages) {
        List<BaseComponent> baseComponents = new ArrayList<>();
        for (String message : messages) {
            baseComponents.addAll(Arrays.asList(toBaseComponent(message + "\n")));
        }
        return baseComponents.toArray(new BaseComponent[0]);
    }

    /**
     * Sets the current part back to normal settings. Only text is kept.
     *
     * @return this ComponentBuilder for chaining
     */
    public TextBuilder reset() {
        return retain(FormatRetention.NONE);
    }

    /**
     * Retains only the specified formatting. Text is not modified.
     *
     * @param retention the formatting to retain
     * @return this ComponentBuilder for chaining
     */
    public TextBuilder retain(TextBuilder.FormatRetention retention) {
        BaseComponent previous = current;

        switch (retention) {
            case NONE:
                current = new TextComponent(current.getText());
                break;
            case ALL:
                // No changes are required
                break;
            case EVENTS:
                current = new TextComponent(current.getText());
                current.setInsertion(previous.getInsertion());
                current.setClickEvent(previous.getClickEvent());
                current.setHoverEvent(previous.getHoverEvent());
                break;
            case FORMATTING:
                current.setClickEvent(null);
                current.setHoverEvent(null);
                break;
        }
        return this;
    }

    /**
     * Returns the components needed to display the message created by this
     * builder.
     *
     * @return the created components
     */
    public BaseComponent[] create() {
        parts.add(current);
        return parts.toArray(new BaseComponent[parts.size()]);
    }

    public void send(Player player) {
        final BaseComponent[] baseComponents = create();
        player.spigot().sendMessage(baseComponents);
    }


    public void send(Player player,BaseComponent[] baseComponents) {
        player.spigot().sendMessage(baseComponents);
    }
    public void send(Player... players) {
        this.send(false, players);
    }

    public void send(boolean sendConsole, Player... players) {
        final BaseComponent[] baseComponents = create();
        for (Player player : players) {
            this.send(player, baseComponents);
        }
        if (sendConsole) {
            Bukkit.getConsoleSender().sendMessage(TextComponent.toPlainText(baseComponents));
        }
    }
    public void send(boolean sendConsole, Collection<? extends Player> players) {
        final BaseComponent[] baseComponents = create();
        for (Player player : players) {
            this.send(player, baseComponents);
        }
        if (sendConsole) {
            Bukkit.getConsoleSender().sendMessage(TextComponent.toPlainText(baseComponents));
        }
    }

    public void send(Collection<? extends Player> players) {
        this.send(false, players);
    }

    public void broadcast() {
        this.send(true, Bukkit.getOnlinePlayers());
    }

    public enum FormatRetention {

        /**
         * Specify that we do not want to retain anything from the previous component.
         */
        NONE,
        /**
         * Specify that we want the formatting retained from the previous component.
         */
        FORMATTING,
        /**
         * Specify that we want the events retained from the previous component.
         */
        EVENTS,
        /**
         * Specify that we want to retain everything from the previous component.
         */
        ALL
    }

}