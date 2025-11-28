class Environment(
    private val enclosing: Environment? = null // for nested block scopes
) {
    private val values = mutableMapOf<String, Any?>()

    // declare a new variable in current scope
    fun define(name: String, value: Any?) {
        values[name] = value
    }

    // retrieve a variable value
    fun get(name: Token): Any? {
        if (values.containsKey(name.text)) {
            return values[name.text]
        }

        // check outer scope
        if (enclosing != null) return enclosing.get(name)

        error("[line ${name.line}] Runtime error: Undefined variable '${name.text}'.")
    }

    // assign new value to existing variable
    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.text)) {
            values[name.text] = value
            return
        }

        // if not here, maybe it's in parent scope
        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        error("[line ${name.line}] Runtime error: Cannot assign to undeclared variable '${name.text}'.")
    }
}
