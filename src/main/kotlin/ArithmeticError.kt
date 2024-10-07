class ArithmeticError(
    override val token: Token,
    override val message: String
): RuntimeError(token, message)