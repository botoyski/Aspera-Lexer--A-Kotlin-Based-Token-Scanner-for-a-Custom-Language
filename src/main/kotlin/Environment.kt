class Environment(private val enclosing: Environment? = null) {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun assign(name: Token, value: Any?) {
        if (name.text in values) {
            values[name.text] = value
            return
        }
        enclosing?.assign(name, value) ?: error("Undefined variable '${name.text}'.")
    }

    fun get(name: Token): Any? {
        if (name.text in values) return values[name.text]
        enclosing?.let { return it.get(name) }
        error("Undefined variable '${name.text}'.")
    }
}

interface Callable {
    fun arity(): Int
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}
