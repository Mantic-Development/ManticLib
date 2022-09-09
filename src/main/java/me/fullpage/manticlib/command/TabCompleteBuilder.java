package me.fullpage.manticlib.command;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class TabCompleteBuilder {

    public static TabCompleteBuilder create(String[] args, CommandSender sender) {
        return new TabCompleteBuilder(args, sender);
    }

    /**
     * @see #create(String[], CommandSender)
     */
    @Deprecated
    public static TabCompleteBuilder create(String[] args) {
        return new TabCompleteBuilder(args);
    }

    private String[] args;
    private CommandSender sender;
    private TabCompleteElement[] elements;

    /**
     * @see TabCompleteBuilder#create(String[], CommandSender)
     */
    @Deprecated
    private TabCompleteBuilder(String[] args) {
        this.args = args;
        this.elements = new TabCompleteElement[0];
    }
    private TabCompleteBuilder(String[] args, CommandSender sender) {
        this.args = args;
        this.sender = sender;
        this.elements = new TabCompleteElement[0];
    }

    public TabCompleteBuilder add(TabCompleteElement element) {
        TabCompleteElement[] newElements = new TabCompleteElement[this.elements.length + 1];
        System.arraycopy(this.elements, 0, newElements, 0, this.elements.length);
        newElements[this.elements.length] = element;
        this.elements = newElements;
        return this;
    }

    public List<String> build() {
        List<String> results = new ArrayList<>();
        for (TabCompleteElement element : this.elements) {
            if (element.hasPermission(sender) && element.getIndex() == this.args.length-1) {
                String arg = this.args[element.getIndex()].toLowerCase();
                for (String result : element.getResults()) {
                    if (result!= null && result.toLowerCase().startsWith(arg)) {
                        results.add(result);
                    }
                }
                return results;
            }
        }
        return results;
    }

}
