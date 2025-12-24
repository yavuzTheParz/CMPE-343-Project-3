package main;

public class Member3NumberPolicyTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        Member3NumberPolicy p = new Member3NumberPolicy();

        assertFail(p.parsePositiveDouble(null, "kg"), "kg cannot be empty.");
        assertFail(p.parsePositiveDouble("", "kg"), "kg cannot be empty.");
        assertFail(p.parsePositiveDouble(" ", "kg"), "kg cannot be empty.");
        assertOk(p.parsePositiveDouble(" 5 ", "kg"), 5);

        assertOk(p.parsePositiveDouble("1.25", "kg"), 1.25);
        assertOk(p.parsePositiveDouble("1,25", "kg"), 1.25);
        assertFail(p.parsePositiveDouble("1,2,3", "kg"), "Invalid number format.");

        assertFail(p.parsePositiveDouble("NaN", "kg"), "Invalid number format.");
        assertFail(p.parsePositiveDouble("Infinity", "kg"), "Invalid number format.");

        assertFail(p.parsePositiveDouble("0", "kg"), "kg must be greater than 0.");
        assertFail(p.parsePositiveDouble("-1", "kg"), "kg must be greater than 0.");

        assertOk(p.parseNonNegativeDouble("0", "kg"), 0);
        assertFail(p.parseNonNegativeDouble("-0.1", "kg"), "kg cannot be negative.");

        assertFail(p.parseNonNegativeInt("3.2", "count"), "count must be a number.");
        assertFail(p.parseNonNegativeInt("-1", "count"), "count cannot be negative.");

        assertOk(p.parseRating1to5("5"), 5);
        assertFail(p.parseRating1to5("6"), "Rating must be an integer between 1 and 5.");

        if (failed > 0) {
            throw new RuntimeException("TESTS FAILED");
        }
        System.out.println("ALL TESTS PASSED (" + passed + ")");
    }

    private static void assertOk(Member3NumberPolicy.ParseResult r, double expected) {
        if (!r.ok || Math.abs(r.value - expected) > 1e-9) {
            failed++;
            throw new RuntimeException("Expected OK: " + expected);
        }
        passed++;
    }

    private static void assertFail(Member3NumberPolicy.ParseResult r, String msg) {
        if (r.ok || !msg.equals(r.error)) {
            failed++;
            throw new RuntimeException("Expected FAIL: " + msg);
        }
        passed++;
    }
}
