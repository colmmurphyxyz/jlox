class Environment(
    val enclosing: Environment?
) {

    // secondary constructor
    constructor() : this(null)

    private val values = HashMap<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    private fun ancestor(distance: Int): Environment {
        return if (distance == 0) {
            this
        } else {
            enclosing!!.ancestor(distance - 1)
        }
    }

    fun getAt(distance: Int, name: String): Any? {
        return ancestor(distance).values[name]
    }

    fun assignAt(distance: Int, name: Token, value: Any) {
        ancestor(distance).values[name.lexeme] = value
    }

    fun assign(name: Token, value: Any) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        throw RuntimeError(
            name,
            "Undefined variable '${name.lexeme}'."
        )
    }

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }

        // if name does not exist, retrieve it from the enclosing environment, recursively
        if (enclosing != null) return enclosing.get(name)

        throw RuntimeError(
            name,
            "Undefined variable '${name.lexeme}'s"
        )
    }
}