package me.fullpage.manticlib.command;

import lombok.Getter;

@Getter
public class TabCompleteElement {

    private final int index;
    private final String[] results;

    public TabCompleteElement(int index, String... results) {
        this.index = index;
        this.results = results;
    }



}
