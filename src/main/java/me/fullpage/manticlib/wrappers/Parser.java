package me.fullpage.manticlib.wrappers;

public abstract class Parser {

    public static final Parser EMPTY = new Parser() {
        @Override
        public String parse(String toParse) {
            return toParse;
        }
    };

    public abstract String parse(String toParse);

}