import TokenType.*

class HtmlFormatter(
    private val tokens: List<Token>
) {
    private val header = """
        <!doctype html>
        <html>
        <head>
        </head>
        <body>
        <pre>
    """.trimIndent()

    private val footer = """
        </pre>
        </body>
        </html>
    """.trimIndent()

    private val keywords = setOf<TokenType>(
        AND,
        CLASS,
        ELSE,
        FALSE,
        FUN,
        FOR,
        IF,
        NIL,
        OR,
        PRINT,
        RETURN,
        SUPER,
        THIS,
        TRUE,
        VAR,
        WHILE
    )

    fun format(): String {
        var html = header
        var currentLine = -1
        val iter = tokens.iterator()
        while (iter.hasNext()) {
            val token = iter.next()
            if (token.line > currentLine) {
                currentLine = token.line
                html += "\n"
            }
            val stringified = token.lexeme.let {
                if (keywords.contains(token.type)) it.bold() else it
            }
            html += stringified
        }

        return html + footer
    }

    private fun String.bold(): String =
        "<b>$this</b>"

    private fun String.italic(): String =
        "<i>$this</i>"

    private fun String.red(): String =
        "<span style=\"color: #ff0000\">$this</span"
}