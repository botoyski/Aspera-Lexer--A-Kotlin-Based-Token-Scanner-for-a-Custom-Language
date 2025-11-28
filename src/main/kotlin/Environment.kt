class Environment(
    private val enclosing: Environment? = null
) {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        if (values.containsKey(name.text)) {
            return values[name.text]
        }
        if (enclosing != null) return enclosing.get(name)
        error("[line ${name.line}] Runtime error: Undefined variable '${name.text}'.")
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.text)) {
            values[name.text] = value
            return
        }
        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }
        error("[line ${name.line}] Runtime error: Cannot assign to undeclared variable '${name.text}'.")
    }
}
