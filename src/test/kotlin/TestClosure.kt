import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestClosure {
    @Test
    fun testClosure() {
        val output = Lox.runFileWithOutput("""
            var f;
            var g;

            {
              var local = "local";
              fun f_() {
                print local;
                local = "after f";
                print local;
              }
              f = f_;

              fun g_() {
                print local;
                local = "after g";
                print local;
              }
              g = g_;
            }

            f();
            // expect: local
            // expect: after f

            g();
            // expect: after f
            // expect: after g
        """.trimIndent())

        assertEquals("local\nafter f\nafter f\nafter g\n", output)
    }

    @Test
    fun testToAssignedLater() {
        val output = Lox.runFileWithOutput("""
            var a = "global";

            {
              fun assign() {
                a = "assigned";
              }

              var a = "inner";
              assign();
              print a; // expect: inner
            }

            print a; // expect: assigned
        """.trimIndent())

        assertEquals("inner\nassigned\n", output)
    }

    @Test
    fun testCloseOverFunctionParameter() {
        val output = Lox.runFileWithOutput("""
            var f;

            fun foo(param) {
              fun f_() {
                print param;
              }
              f = f_;
            }
            foo("param");

            f(); // expect: param
        """.trimIndent())

        assertEquals("param\n", output)
    }

    @Test
    fun testOverLaterVariable() {
        val output = Lox.runFileWithOutput("""
            fun f() {
              var a = "a";
              var b = "b";
              fun g() {
                print b; // expect: b
                print a; // expect: a
              }
              g();
            }
            f();
        """.trimIndent())

        assertEquals("b\na\n", output)
    }

    @Test
    fun testCloseOverMethodParameter() {
        val output = Lox.runFileWithOutput("""
            var f;

            class Foo {
              method(param) {
                fun f_() {
                  print param;
                }
                f = f_;
              }
            }

            Foo().method("param");
            f(); // expect: param
        """.trimIndent())

        assertEquals("param\n", output)
    }

    @Test
    fun testCloseClosureInFunction() {
        val output = Lox.runFileWithOutput("""
            var f;

            {
              var local = "local";
              fun f_() {
                print local;
              }
              f = f_;
            }

            f(); // expect: local
        """.trimIndent())
        assertEquals("local\n", output)
    }

    @Test
    fun testNestedClosure() {
        val output = Lox.runFileWithOutput("""
            var f;

            fun f1() {
              var a = "a";
              fun f2() {
                var b = "b";
                fun f3() {
                  var c = "c";
                  fun f4() {
                    print a;
                    print b;
                    print c;
                  }
                  f = f4;
                }
                f3();
              }
              f2();
            }
            f1();

            f();
            // expect: a
            // expect: b
            // expect: c
        """.trimIndent())

        assertEquals("a\nb\nc\n", output)
    }

    @Test
    fun testOpenClosureInFunction() {
        val output = Lox.runFileWithOutput("""
            {
              var local = "local";
              fun f() {
                print local; // expect: local
              }
              f();
            }
        """.trimIndent())
        assertEquals("local\n", output)
    }

    @Test
    fun testReferenceClosureMultipleTimes() {
        val output = Lox.runFileWithOutput("""
            var f;

            {
              var a = "a";
              fun f_() {
                print a;
                print a;
              }
              f = f_;
            }

            f();
            // expect: a
            // expect: a
        """.trimIndent())

        assertEquals("a\na\n", output)
    }

    @Test
    fun testReuseClosureSlot() {
        val output = Lox.runFileWithOutput("""
            {
              var f;

              {
                var a = "a";
                fun f_() { print a; }
                f = f_;
              }

              {
                // Since a is out of scope, the local slot will be reused by b. Make sure
                // that f still closes over a.
                var b = "b";
                f(); // expect: a
              }
            }
        """.trimIndent())
        assertEquals("a\n", output)
    }

    @Test
    fun testShadowClosureWithLocal() {
        val output = Lox.runFileWithOutput("""
            {
              var foo = "closure";
              fun f() {
                {
                  print foo; // expect: closure
                  var foo = "shadow";
                  print foo; // expect: shadow
                }
                print foo; // expect: closure
              }
              f();
            }
        """.trimIndent())
        assertEquals("closure\nshadow\nclosure\n", output)
    }

    @Test
    fun testUnusedClosure() {
        val output = Lox.runFileWithOutput("""
            {
              var a = "a";
              if (false) {
                fun foo() { a; }
              }
            }

            // If we get here, we didn't segfault when a went out of scope.
            print "ok"; // expect: ok
        """.trimIndent())

        assertEquals("ok\n", output)
    }

    @Test
    fun testUnusedLaterClosure() {
        val output = Lox.runFileWithOutput("""
            var closure;

            {
              var a = "a";

              {
                var b = "b";
                fun returnA() {
                  return a;
                }

                closure = returnA;

                if (false) {
                  fun returnB() {
                    return b;
                  }
                }
              }

              print closure(); // expect: a
            }
        """.trimIndent())
        assertEquals("a\n", output)
    }
}