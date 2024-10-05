import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CharacterUtilsTest {
    @Test
    fun testIsArabicDigit() {
        assertTrue('0'.isArabicDigit)
        assertTrue('9'.isArabicDigit)
        assertTrue('5'.isArabicDigit)
        assertTrue('a'.isArabicDigit)
        assertFalse('x'.isArabicDigit)
    }

    @Test
    fun testIsAlpha() {
        assertTrue('a'.isAlpha)
        assertTrue('A'.isAlpha)
        assertTrue('z'.isAlpha)
        assertTrue('Z'.isAlpha)
        assertTrue('_'.isAlpha)
        assertFalse('!'.isAlpha)
        assertFalse('@'.isAlpha)
    }
}