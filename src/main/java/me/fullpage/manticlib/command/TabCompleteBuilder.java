package me.fullpage.manticlib.command;

import java.util.ArrayList;
import java.util.List;

public class TabCompleteBuilder {

    public static TabCompleteBuilder create(String[] args) {
        return new TabCompleteBuilder(args);
    }

    private String[] args;
    private TabCompleteElement[] elements;
    private TabCompleteBuilder(String[] args) {
        this.args = args;
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
            if (element.getIndex() == this.args.length-1) {
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
