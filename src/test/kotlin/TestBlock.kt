import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestBlock {
    @Test
    fun testEmptyBlock() {
        val output = Lox.runFileWithOutput("""
            {} // By itself.

            // In a statement.
            if (true) {}
            if (false) {} else {}

            print "ok"; // expect: ok
        """.trimIndent())

        assertEquals("ok\n", output)
    }

    @Test
    fun testBlockScope() {
        val output = Lox.runFileWithOutput("""
            var a = "outer";

            {
              var a = "inner";
              print a; // expect: inner
            }

            print a; // expect: outer
        """.trimIndent())

        assertEquals("inner\nouter\n", output)
    }
}