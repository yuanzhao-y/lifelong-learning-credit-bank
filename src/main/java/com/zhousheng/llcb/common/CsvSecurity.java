package com.zhousheng.llcb.common;

public final class CsvSecurity {

    private CsvSecurity() {
    }

    public static String cell(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (isFormula(text)) {
            text = "'" + text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private static boolean isFormula(String value) {
        if (value.isEmpty()) {
            return false;
        }
        char first = value.charAt(0);
        return first == '=' || first == '+' || first == '-' || first == '@'
                || first == '\t' || first == '\r';
    }
}
