import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

import TokenType.*
import kotlin.test.assertContentEquals

class ScannerTest {

    @Test
    fun testInlineComments() {
        val source = "var x = 123 // this is a comment and should be discarded"
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        assertEquals(tokens[tokens.lastIndex - 1].type, NUMBER)
    }

    @Test
    fun testMultipleInlineComments() {
        val source = "123 // comment // second comment"
        val tokens = Scanner(source).scanTokens()
        assertEquals(tokens.size, 2) // literal + EOF
        assertEquals(tokens[0].type, NUMBER)
    }

    @Test
    fun testBlockComments() {
        // last token of source should be on line 5
        val source = """
            var x = 123
            print x /* inline comment
            still a comment
            last line of comments */ var y = 5
        """.trimIndent()

        val tokens = Scanner(source).scanTokens()
        val lastToken = tokens[tokens.lastIndex - 1]

        assertEquals(4, lastToken.line)
        assertEquals(5.0, lastToken.literal)
    }

    @Test
    fun testScanInlineBlockComment() {
        val source = "var x/* ... */ = 3"
        val tokens = Scanner(source).scanTokens()
        val types = tokens.map { it.type }.toTypedArray()
        assertContentEquals(arrayOf(VAR, IDENTIFIER, EQUAL, NUMBER, EOF), types)
    }

    @Test
    fun testMaximalMunch() {
        // test that the identifier 'orchid' is *not* scanner as the keyword `or` followed by 'chid'
        val tokens = Scanner("var orchid = \"flower\"").scanTokens()
        val orchidToken = tokens[1]
        assertEquals(Token(IDENTIFIER, "orchid", null, 1), orchidToken)
        assertEquals(
            0,
            tokens.filter { it.type == OR }.size,
            "Lexer scanned an OR token where no such token was present in the source"
        )

    }
}