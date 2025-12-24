package main;

import java.util.Locale;

public class Member3NumberPolicy {

    public static final class ParseResult {
        public final boolean ok;
        public final double value;
        public final String error;

        private ParseResult(boolean ok, double value, String error) {
            this.ok = ok;
            this.value = value;
            this.error = error;
        }

        public static ParseResult ok(double value) {
            return new ParseResult(true, value, null);
        }

        public static ParseResult fail(String error) {
            return new ParseResult(false, 0.0, error);
        }
    }

    public ParseResult parsePositiveDouble(String input, String fieldName) {
        ParseResult base = parseDoubleCommon(input, fieldName);
        if (!base.ok) return base;

        if (base.value <= 0.0) {
            return ParseResult.fail(fieldName + " must be greater than 0.");
        }
        return base;
    }

    public ParseResult parseNonNegativeDouble(String input, String fieldName) {
        ParseResult base = parseDoubleCommon(input, fieldName);
        if (!base.ok) return base;

        if (base.value < 0.0) {
            return ParseResult.fail(fieldName + " cannot be negative.");
        }
        return base;
    }

    public ParseResult parseNonNegativeInt(String input, String fieldName) {
        String trimmed = trimOrNull(input);
        if (trimmed == null) {
            return ParseResult.fail(fieldName + " cannot be empty.");
        }

        if (!trimmed.matches("[+-]?\\d+")) {
            return ParseResult.fail(fieldName + " must be a number.");
        }

        try {
            long v = Long.parseLong(trimmed);
            if (v < 0L) return ParseResult.fail(fieldName + " cannot be negative.");
            if (v > Integer.MAX_VALUE) return ParseResult.fail("Invalid number format.");
            return ParseResult.ok((int) v);
        } catch (Exception e) {
            return ParseResult.fail("Invalid number format.");
        }
    }

    public ParseResult parseRating1to5(String input) {
        String trimmed = trimOrNull(input);
        if (trimmed == null) {
            return ParseResult.fail("Rating must be an integer between 1 and 5.");
        }

        if (!trimmed.matches("[+-]?\\d+")) {
            return ParseResult.fail("Rating must be an integer between 1 and 5.");
        }

        try {
            int v = Integer.parseInt(trimmed);
            if (v < 1 || v > 5) {
                return ParseResult.fail("Rating must be an integer between 1 and 5.");
            }
            return ParseResult.ok(v);
        } catch (Exception e) {
            return ParseResult.fail("Rating must be an integer between 1 and 5.");
        }
    }

    // -------- helpers --------

    private ParseResult parseDoubleCommon(String input, String fieldName) {
        String trimmed = trimOrNull(input);
        if (trimmed == null) {
            return ParseResult.fail(fieldName + " cannot be empty.");
        }

        String normalized = trimmed.replace(',', '.');

        if (countChar(normalized, '.') > 1) {
            return ParseResult.fail("Invalid number format.");
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.equals("nan") || lower.equals("infinity")
                || lower.equals("-infinity") || lower.equals("+infinity")) {
            return ParseResult.fail("Invalid number format.");
        }

        try {
            double v = Double.parseDouble(normalized);
            if (Double.isNaN(v) || Double.isInfinite(v)) {
                return ParseResult.fail("Invalid number format.");
            }
            return ParseResult.ok(v);
        } catch (Exception e) {
            return ParseResult.fail(fieldName + " must be a number.");
        }
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        return t;
    }

    private int countChar(String s, char c) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) n++;
        }
        return n;
    }
}
