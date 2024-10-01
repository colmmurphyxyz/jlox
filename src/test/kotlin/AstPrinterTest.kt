import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class AstPrinterTest {

    @Test
    fun testPrintExpression() {
        val expression = Expr.Binary(
            Expr.Unary(
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal(123)),
            Token(TokenType.STAR, "*", null, 1),
            Expr.Grouping(
                Expr.Literal(45.67)
            )
        )

        assertEquals("(* (- 123) (group 45.67))", AstPrinter().print(expression))
    }
}