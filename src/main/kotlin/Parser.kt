import java.util.ArrayList

import TokenType.*

class Parser(
    private val tokens: List<Token>
) {
    private var current: Int = 0

    companion object {
        private class ParseError : RuntimeException()
    }

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd) {
            declaration()?.let {
                statements.add(it)
            }
        }

        return statements
    }

    private fun expression(): Expr {
        return assignment()
    }

    private fun declaration(): Stmt? {
        try {
            if (match(VAR)) return varDeclaration()

            return statement()
        } catch (error: ParseError) {
            synchronize()
            return null
        }
    }

    private fun statement(): Stmt {
        if (match(IF)) return ifStatement()
        if (match(PRINT)) return printStatement()
        if (match(LEFT_BRACE)) return Stmt.Block(block())
        return expressionStatement()
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' adter if condition.")

        val thenBranch = statement()
        var elseBranch: Stmt? = null
        if (match(ELSE)) {
            elseBranch = statement()
        }

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun varDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect variable name.")
        var initializer: Expr? = null
        if (match(EQUAL)) {
            initializer = expression()
        }
        consume(SEMICOLON, "Expect ';' after variable declaration")
        return Stmt.Var(name, initializer)
    }

    private fun expressionStatement(): Stmt {
        val expr = assignment()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Expression(expr)
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!check(RIGHT_BRACE) && !isAtEnd) {
            declaration()?.also {
                statements.add(it)
            }
        }

        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun assignment(): Expr {
        val expr = ternary()

        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }

            error(equals, "Invalid assignment target.")
        }

        return expr
    }

    private fun ternary(): Expr {
        var expr = equality()

        if (match(QUESTION_MARK)) {
            val left = expression()
            consume(COLON, "Expect ':' after '?'")
            val right = expression()
            expr = Expr.Ternary(expr, left, right)
        }

        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = term()
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr = factor()
        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expr {
        var expr = unary()
        while (match(SLASH, STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(PLUS, STAR, SLASH)) {
            throw error(previous(), "Unexpected binary operator.")
        }
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return primary()
    }

    private fun primary(): Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NIL)) return Expr.Literal(Nil)

        if (match(NUMBER, STRING)) {
            return Expr.Literal(previous().literal)
        }

        if (match(IDENTIFIER)) {
            return Expr.Variable(previous())
        }

        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression")
            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expect expression")
     }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) {
            return advance()
        }
        throw error(peek(), message)
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd) current++
        return previous()
    }

    private val isAtEnd: Boolean
        get() = peek().type == EOF

    private fun peek(): Token =
        tokens[current]

    private fun previous(): Token =
        tokens[current - 1]

    private fun error(token: Token, message: String): ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd) {
            if (previous().type == SEMICOLON) return

            when (peek().type) {
                CLASS,
                FUN,
                VAR,
                FOR,
                IF,
                WHILE,
                PRINT,
                RETURN -> return
                else -> {}
            }

            advance()
        }
    }
}