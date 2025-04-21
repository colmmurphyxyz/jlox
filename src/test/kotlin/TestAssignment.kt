import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestAssignment {
    @Test
    fun testAssociativity() {
        val output = Lox.runFileWithOutput("""
            var a = "a";
            var b = "b";
            var c = "c";
            
            // Assignment is right-associative.
            a = b = c;
            print a; // expect: c
            print b; // expect: c
            print c; // expect: c
        """.trimIndent())

        assertEquals("c\nc\nc\n", output)
    }

    @Test
    fun testGlobal() {
        val output = Lox.runFileWithOutput("""
            var a = "before";
            print a; // expect: before

            a = "after";
            print a; // expect: after

            print a = "arg"; // expect: arg
            print a; // expect: arg
        """.trimIndent())

        assertEquals("before\nafter\narg\narg\n", output)
    }

    @Test
    fun testGrouping() {
        val output = Lox.runFileWithOutput("""
            var a = "a";
            (a) = "value"; // Error at '=': Invalid assignment target.
        """.trimIndent())

        assertEquals("Error at '=': Invalid assignment target.", output)
    }

    @Test
    fun testInfixOperator() {
        val output = Lox.runFileWithOutput("""
            var a = "a";
            var b = "b";
            a + b = "value"; // Error at '=': Invalid assignment target.
        """.trimIndent())

        assertEquals("Error at '=': Invalid assignment target.", output)
    }

    @Test
    fun testLocal() {
        val output = Lox.runFileWithOutput("""
            {
              var a = "before";
              print a; // expect: before

              a = "after";
              print a; // expect: after

              print a = "arg"; // expect: arg
              print a; // expect: arg
            }
        """.trimIndent())

        assertEquals("before\nafter\narg\narg\n", output)
    }

    @Test
    fun testPrefixOperator() {
        val output = Lox.runFileWithOutput("""
            var a = "a";
            !a = "value"; // Error at '=': Invalid assignment target.
        """.trimIndent())

        assertEquals("Error at '=': Invalid assignment target.", output)
    }

    @Test
    fun testAssignmentOnRHS() {
        val output = Lox.runFileWithOutput("""
            // Assignment on RHS of variable.
            var a = "before";
            var c = a = "var";
            print a; // expect: var
            print c; // expect: var
        """.trimIndent())

        assertEquals(output, "var\nvar\n")
    }

    @Test
    fun testAssignToThis() {
        val output = Lox.runFileWithOutput("""
            class Foo {
              Foo() {
                this = "value"; // Error at '=': Invalid assignment target.
              }
            }

            Foo();
        """.trimIndent())

        assertEquals("Error at '=': Invalid assignment target.", output)
    }

    @Test
    fun testAssignToUndeclaredVariable() {
        val output = Lox.runFileWithOutput("""
            unknown = "what"; // expect runtime error: Undefined variable 'unknown'.
        """.trimIndent())
        assertEquals(output, "Undefined variable 'unknown'.")
    }
}