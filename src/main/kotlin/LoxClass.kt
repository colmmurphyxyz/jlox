class LoxClass(
    val name: String,
    val superclass: LoxClass?,
    private val methods: Map<String, LoxFunction>
) : LoxCallable {
    override fun toString(): String = name

    fun findMethod(name: String): LoxFunction? {
        if (methods.containsKey(name)) {
            return methods[name]
        }
        if (superclass != null) {
            return superclass.findMethod(name)
        }
        return null
    }

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any? {
        val instance = LoxInstance(this)
        findMethod("init")?.bind(instance)?.call(interpreter, arguments)
        return instance
    }

    override val arity: Int
        get() = findMethod("init")?.arity ?: 0
}