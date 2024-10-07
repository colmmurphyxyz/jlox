open class RuntimeError(
    open val token: Token,
    override val message: String
): RuntimeException(message)