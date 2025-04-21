import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestBool {
    private fun toBool(s: String): Boolean
        = when (s) {
            "true" -> true
            "false" -> false
            else -> throw IllegalArgumentException("Invalid boolean string: $s")
        }

    @Test
    fun testEquality() {
        val output = Lox.runFileWithOutput("""
            print true == true;    // expect: true
            print true == false;   // expect: false
            print false == true;   // expect: false
            print false == false;  // expect: true

            // Not equal to other types.
            print true == 1;        // expect: false
            print false == 0;       // expect: false
            print true == "true";   // expect: false
            print false == "false"; // expect: false
            print false == "";      // expect: false

            print true != true;    // expect: false
            print true != false;   // expect: true
            print false != true;   // expect: true
            print false != false;  // expect: false

            // Not equal to other types.
            print true != 1;        // expect: true
            print false != 0;       // expect: true
            print true != "true";   // expect: true
            print false != "false"; // expect: true
            print false != "";      // expect: true
        """.trimIndent()).split("\n").dropLast(1).map(this::toBool)
        val expected = listOf(
            true, false, false, true,
            false, false, false, false, false,
            false, true, true, false,
            true, true, true, true, true
        )
        for (i in output.indices) {
            assert(output[i] == expected[i]) { "Expected ${expected[i]} but got ${output[i]} at index $i" }
        }
    }

    @Test
    fun testBooleanNegation() {
        val output = Lox.runFileWithOutput("""
            print !true;    // expect: false
            print !false;   // expect: true
            print !!true;   // expect: true
        """.trimIndent())

        assertEquals("false\ntrue\ntrue\n", output)
    }
}