val Char.isArabicDigit: Boolean
    get() = this in '0'..'9'

val Char.isAlpha: Boolean
    get() = this in 'a' .. 'z' || this in 'A' .. 'Z' || this == '_'

val Char.isAlphaNumeric: Boolean
    get() = this.isArabicDigit || this.isAlpha