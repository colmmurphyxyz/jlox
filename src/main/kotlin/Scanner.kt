import TokenType.*
class Scanner(
    private val source: String
) {
    private var tokens: ArrayList<Token> = ArrayList()
    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        val c: Char = advance()
        when (c) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> {
                addToken(if (match('=')) BANG_EQUAL else BANG)
            }
            '='-> {
                addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            }
            '<' -> {
                addToken(if (match('=')) LESS_EQUAL else LESS)
            }
            '>' -> {
                addToken(if (match('=')) GREATER_EQUAL else GREATER)
            }
            '/' -> {
                if (match('/')) {
                    // a comment goes until the end of  the line
                    while (peek() !='\n' && !isAtEnd()) {
                        advance()
                    }
                } else {
                    addToken(SLASH)
                }
            }
            // meaningless characters (Lox does not have semantic whitespace)
            ' ' -> {}
            '\r' -> {}
            '\t' -> {}
            '\n' -> line++
            else -> {
                Lox.error(line, "Unexpected character.")
            }
        }
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    private fun peek(): Char {
        if (isAtEnd()) return 0.toChar()
        return source[current]
    }

    private fun isAtEnd(): Boolean =
        current >= source.length

    private fun advance(): Char =
        source[current++]

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start..<current)
        tokens.add(Token(type, text, literal, line))
    }
}