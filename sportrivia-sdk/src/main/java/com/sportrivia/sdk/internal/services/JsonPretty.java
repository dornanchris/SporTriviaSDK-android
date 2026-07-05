package com.sportrivia.sdk.internal.services;

/**
 * Re-indents a compact JSON string (as produced by {@code JSONStringer}) into
 * a human-readable form — one field per line, 2-space indent, nested
 * objects/arrays expanded, empty {@code {}}/{@code []} left inline.
 *
 * <p>Purely mechanical: key order and content are preserved exactly, so the
 * output parses back to the same document. Kept dependency-free (no android.*)
 * so it is unit-testable on a plain JVM.
 */
public final class JsonPretty {

    private static final String INDENT_UNIT = "  ";

    private JsonPretty() {}

    public static String indent(String compact) {
        if (compact == null || compact.isEmpty()) {
            return compact;
        }
        StringBuilder out = new StringBuilder(compact.length() + compact.length() / 4);
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < compact.length(); i++) {
            char c = compact.charAt(i);

            if (inString) {
                out.append(c);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            switch (c) {
                case '"':
                    inString = true;
                    out.append(c);
                    break;
                case '{':
                case '[':
                    // Keep empty containers inline: {} and []
                    char close = (c == '{') ? '}' : ']';
                    char next = nextNonSpace(compact, i + 1);
                    if (next == close) {
                        out.append(c).append(close);
                        i = compact.indexOf(close, i + 1);
                        break;
                    }
                    depth++;
                    out.append(c).append('\n').append(repeat(depth));
                    break;
                case '}':
                case ']':
                    depth--;
                    out.append('\n').append(repeat(depth)).append(c);
                    break;
                case ',':
                    out.append(c).append('\n').append(repeat(depth));
                    break;
                case ':':
                    out.append(": ");
                    break;
                default:
                    out.append(c);
                    break;
            }
        }
        return out.toString();
    }

    private static char nextNonSpace(String s, int from) {
        for (int i = from; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != ' ' && c != '\n' && c != '\t' && c != '\r') {
                return c;
            }
        }
        return '\0';
    }

    private static String repeat(int depth) {
        StringBuilder sb = new StringBuilder(depth * INDENT_UNIT.length());
        for (int i = 0; i < depth; i++) {
            sb.append(INDENT_UNIT);
        }
        return sb.toString();
    }
}
