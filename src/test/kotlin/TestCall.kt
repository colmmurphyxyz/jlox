import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestCall {
    @Test
    fun testCallBoolean() {
        val output = Lox.runFileWithOutput("""
            true(); // expect runtime error: Can only call functions and classes.
        """.trimIndent()
        )
        assertEquals("Can only call functions and classes.", output)
    }

    @Test
    fun testCallNil() {
        val output = Lox.runFileWithOutput("""
            nil(); // expect runtime error: Can only call functions and classes.
        """.trimIndent()
        )
        assertEquals("Can only call functions and classes.", output)
    }

    @Test
    fun testCallNumber() {
        val output = Lox.runFileWithOutput("""
            123(); // expect runtime error: Can only call functions and classes.
        """.trimIndent()
        )
        assertEquals("Can only call functions and classes.", output)
    }

    @Test
    fun testCallObjectInstance() {
        val output = Lox.runFileWithOutput("""
            class Foo {}

            var foo = Foo();
            foo(); // expect runtime error: Can only call functions and classes.
        """.trimIndent())
        assertEquals("Can only call functions and classes.", output)
    }

    @Test
    fun testCallString() {
        val output = Lox.runFileWithOutput("""
            "foo"(); // expect runtime error: Can only call functions and classes.
        """.trimIndent()
        )
        assertEquals("Can only call functions and classes.", output)
    }
}