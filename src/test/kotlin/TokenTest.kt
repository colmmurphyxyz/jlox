import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

import TokenType.*
import kotlin.test.assertContains

class TokenTest {

    private lateinit var token: Token

    @BeforeEach
    fun createToken() {
        token = Token(STRING, "string literal", "string literal", 42)
    }

    @Test
    fun testToString() {
        val str = token.toString()
        assertContains(str, token.lexeme)
        assertContains(str, token.type.toString())
        assertContains(str, token.line.toString())
        assertContains(str, token.literal as String)
    }

    @Test
    fun getType() {
        assertEquals(STRING, token.type)
    }

    @Test
    fun getLexeme() {
        assertEquals("string literal", token.lexeme)
    }

    @Test
    fun getLiteral() {
        assertEquals("string literal", token.literal)
        assertEquals(token.literal, token.lexeme)
    }

    @Test
    fun getLine() {
        assertEquals(42, token.line)
    }
}