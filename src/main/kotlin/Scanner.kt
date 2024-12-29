import TokenType.*
class Scanner(
    private val source: String
) {
    private var tokens: ArrayList<Token> = ArrayList()
    private var start = 0
    private var current = 0
    private var line = 1

    private val keywords: Map<String, TokenType> = mapOf(
        "and" to AND,
        "break" to BREAK,
        "class" to CLASS,
        "else" to ELSE,
        "false" to FALSE,
        "for" to FOR,
        "fun" to FUN,
        "if" to IF,
        "nil" to NIL,
        "or" to OR,
        "print" to PRINT,
        "return" to RETURN,
        "super" to SUPER,
        "this" to THIS,
        "var" to VAR,
        "true" to TRUE,
        "while" to WHILE
    )

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        when (val c: Char = advance()) {
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
            '?' -> addToken(QUESTION_MARK)
            ':' -> addToken(COLON)
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
                if (match('/')) { // inline comment
                    // a comment goes until the end of  the line
                    while (peek() != '\n' && !isAtEnd()) {
                        advance()
                    }
                } else if (match('*')) { // multiline comment
                    while (!(peek() == '*' && peekNext() == '/')) {
                        if (isAtEnd()) {
                            Lox.error(line, "Unterminated multi-line comment")
                        }
                        if (peek() == '\n') line++
                        advance()
                    }

                    // discard multi-line comment terminator "*/"
                    repeat(2) {
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
            // string literals
            '"' -> string()
            else -> {
                if (c.isArabicDigit) {
                    number()
                } else if (c.isAlpha) {
                    identifier()
                } else {
                    Lox.error(line, "Unexpected character.")
                }
            }
        }
    }

    private fun identifier() {
        while (peek().isAlphaNumeric) advance()
        val text = source.substring(start ..< current)
        val type = keywords[text] ?: IDENTIFIER
        addToken(type)
    }

    private fun number() {
        while (peek().isArabicDigit) advance()

        // look for fractional part
        if (peek() == '.' && peekNext().isArabicDigit) {
            advance()

            while (peek().isArabicDigit) advance()
        }

        addToken(
            NUMBER,
            source.substring(start ..< current).toDouble()
            )
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string literal")
            return
        }

        // the closing
        advance()

        val literal = source.substring(start + 1 ..< current - 1)
        addToken(STRING, literal)
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

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return 0.toChar()
        return source[current + 1]
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