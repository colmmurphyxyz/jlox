import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestClass {
    @Test
    fun testEmptyClass() {
        val output = Lox.runFileWithOutput("""
            class Foo {}

            print Foo; // expect: Foo
        """.trimIndent())

        assertEquals("Foo\n", output)
    }

    @Test
    fun testInheritSelf() {
        val output = Lox.runFileWithOutput("""
            class Foo < Foo {} // Error at 'Foo': A class can't inherit from itself.
        """.trimIndent())

        assertEquals("Error at 'Foo': A class can't inherit from itself.", output)
    }

    @Test
    fun testInheritedMethod() {
        val output = Lox.runFileWithOutput("""
            class Foo {
              inFoo() {
                print "in foo";
              }
            }

            class Bar < Foo {
              inBar() {
                print "in bar";
              }
            }

            class Baz < Bar {
              inBaz() {
                print "in baz";
              }
            }

            var baz = Baz();
            baz.inFoo(); // expect: in foo
            baz.inBar(); // expect: in bar
            baz.inBaz(); // expect: in baz
        """.trimIndent())
        assertEquals("in foo\nin bar\nin baz\n", output)
    }

    @Test
    fun testLocalInherit() {
        val output = Lox.runFileWithOutput("""
            class A {}

            fun f() {
              class B < A {}
              return B;
            }

            print f(); // expect: B
        """.trimIndent())
        assertEquals("B\n", output)
    }

    @Test
    fun testLocalInheritOther() {
        val output = Lox.runFileWithOutput("""
            {
              class Foo < Foo {} // Error at 'Foo': A class can't inherit from itself.
            }
            // [c line 5] Error at end: Expect '}' after block.
        """.trimIndent())

        assertEquals("Error at 'Foo': A class can't inherit from itself.", output)
    }

    @Test
    fun testLocalReferenceSelf() {
        val output = Lox.runFileWithOutput("""
            {
              class Foo {
                returnSelf() {
                  return Foo;
                }
              }

              print Foo().returnSelf(); // expect: Foo
            }
        """.trimIndent())
        assertEquals("Foo\n", output)
    }

    @Test
    fun testReferenceSelf() {
        val output = Lox.runFileWithOutput("""
            class Foo {
              returnSelf() {
                return Foo;
              }
            }

            print Foo().returnSelf(); // expect: Foo
        """.trimIndent())
        assertEquals("Foo\n", output)
    }
}