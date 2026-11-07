package org.javajungle;

import java.util.ArrayList;
import java.util.List;

/** Tokenizes Java code so harmless whitespace and comments do not affect checking. */
public final class CodeCleaner {
    private CodeCleaner() {}

    public static String clean(String code) {
        if (code == null) return "";
        return String.join(" ", tokenize(code));
    }

    private static List<String> tokenize(String source) {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if (c == '/' && i + 1 < source.length() && source.charAt(i + 1) == '/') {
                while (i < source.length() && source.charAt(i) != '\n') i++;
            } else if (c == '/' && i + 1 < source.length() && source.charAt(i + 1) == '*') {
                int end = source.indexOf("*/", i + 2);
                i = end < 0 ? source.length() : end + 2;
            } else if (c == '"' || c == '\'') {
                int start = i++;
                while (i < source.length() && source.charAt(i) != c) {
                    if (source.charAt(i) == '\\') i++;
                    i++;
                }
                if (i < source.length()) i++;
                tokens.add(source.substring(start, i));
            } else if (Character.isLetterOrDigit(c) || c == '_' || c == '$') {
                int start = i++;
                while (i < source.length()) {
                    char next = source.charAt(i);
                    if (!Character.isLetterOrDigit(next) && next != '_' && next != '$') break;
                    i++;
                }
                tokens.add(source.substring(start, i));
            } else {
                tokens.add(String.valueOf(c));
                i++;
            }
        }
        return tokens;
    }
}
