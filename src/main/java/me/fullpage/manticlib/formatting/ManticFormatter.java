package me.fullpage.manticlib.formatting;

import me.fullpage.manticlib.utils.Utils;

public class ManticFormatter {

    private final Number input;
    private final FormatType type;

    public ManticFormatter(Number input, FormatType type) {
        this.input = input;
        if (type.equals(FormatType.COMMA)) {
            this.type = FormatType.COMMA;
        } else if (type.equals(FormatType.SHORTENED)) {
            this.type = FormatType.SHORTENED;
        } else {
            this.type = FormatType.RAW;
        }
    }

    public String format() {
        if (type.equals(FormatType.RAW)) {
            return String.valueOf(input);
        } else if (type.equals(FormatType.COMMA)) {
            return Utils.formatNumber(input);
        } else {
            return Utils.formatValue(input.doubleValue());
        }
    }

}
