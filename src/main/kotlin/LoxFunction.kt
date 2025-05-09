class LoxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment,
    private val isInitializer: Boolean = false
): LoxCallable {

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment, isInitializer)
    }

    override val arity = declaration.params.size
    override fun call(interpreter: Interpreter, arguments: List<Any>): Any? {
        val environment = Environment(closure)
        for (i in declaration.params.indices) {
            environment.define(declaration.params[i].lexeme, arguments[i])
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return when (isInitializer) {
                true -> closure.getAt(0, "this")
                false -> returnValue.value
            }
        }

        if (isInitializer) return closure.getAt(0, "this")

        return Nil
    }

    override fun toString(): String =
        "<fn ${declaration.name.lexeme}>"
}