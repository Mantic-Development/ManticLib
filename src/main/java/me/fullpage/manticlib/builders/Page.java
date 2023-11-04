package me.fullpage.manticlib.builders;

import lombok.Getter;
import lombok.Setter;
import me.fullpage.manticlib.string.Txt;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Getter
@Setter
public class Page<T> {

    private String title;
    private Collection<? extends T> collection;
    private ToString<T> toString;
    private ToTextComponent<T> toTextComponent;

    public Page(String title, Collection<? extends T> collection, ToString<T> toString) {
        this.title = title;
        this.collection = collection;
        this.toString = toString;
        this.toTextComponent = null;
    }

    public Page(String title, Collection<? extends T> collection, ToTextComponent<T> toTextComponent) {
        this.title = title;
        this.collection = collection;
        this.toTextComponent = toTextComponent;
        this.toString = null;
    }

    public boolean isValid(int number) {
        boolean result = false;
        if (!collection.isEmpty()) if (number >= 1) {
            result = number <= getMaxPage();
        }
        return result;
    }

    private int getMaxPage() {
        return (int) Math.ceil((double) this.collection.size() / (double) 9);
    }

    public List<T> get(int page) {
        if (!isValid(page)) return new ArrayList<>();
        List<T> items;
        if (this.collection instanceof List) {
            items = (List<T>) this.collection;
        } else {
            items = new ArrayList<>(this.collection);
        }

        int index = page - 1;
        int from = index * 9;
        int to = from + 9;
        if (to > (items).size()) {
            to = (items).size();
        }

        return (items).subList(from, to);
    }

    public List<String> getPage(int page) {
        if (!isValid(page)) return new ArrayList<>();
        List<String> items = new ArrayList<>();
        int index = (page - 1) * 9;
        final List<T> ts = get(page);
        for (Iterator<T> var4 = ts.iterator(); var4.hasNext(); ++index) {
            T pageItem = var4.next();
            if (toString != null) {
                items.add(toString.toString(pageItem, index));
            } else {
                items.add(TextComponent.toLegacyText(toTextComponent.toTextComponent(pageItem, index)));
            }
        }
        return items;
    }

    public List<TextComponent> getTextComponentPage(int page) {
        if (!isValid(page)) return new ArrayList<>();
        List<TextComponent> items = new ArrayList<>();
        int index = (page - 1) * 9;
        final List<T> ts = get(page);
        for (Iterator<T> var4 = ts.iterator(); var4.hasNext(); ++index) {
            T pageItem = var4.next();
            if (toTextComponent != null) {
                items.add(toTextComponent.toTextComponent(pageItem, index));
            } else {
                items.add(new TextComponent(toString.toString(pageItem, index)));
            }
        }
        return items;
    }

    public boolean isTextComponent() {
        return toTextComponent != null;
    }

    public void send(CommandSender sender, int page) {
        sender.sendMessage(Txt.parse(title.replace("{page}", "" + page).replace("{max_page}", "" + getMaxPage())));
        if (isTextComponent()) {
            final List<TextComponent> page1 = getTextComponentPage(page);
            if (page1.isEmpty()) {
                sender.sendMessage("§7None");
            } else {
                for (TextComponent t : page1) {
                    sender.spigot().sendMessage(t);
                }
            }
        } else {
            final List<String> page1 = getPage(page);
            if (page1.isEmpty()) {
                sender.sendMessage("§7None");
            } else {
                for (String t : page1) {
                    sender.sendMessage(t);
                }
            }
        }
    }


    public interface ToString<T> {

        String toString(T entry, int index);

    }


    public interface ToTextComponent<T> {

        TextComponent toTextComponent(T entry, int index);

    }


}
