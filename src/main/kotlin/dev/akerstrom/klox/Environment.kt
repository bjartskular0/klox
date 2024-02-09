package dev.akerstrom.klox

class Environment(val enclosing: Environment? = null) {
    private val values = mutableMapOf<String, Any?>()

    fun get(name: Token): Any? {
        return when {
            values.containsKey(name.lexeme) -> values[name.lexeme]
            enclosing != null -> enclosing.get(name)
            else -> throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }
    }

    fun assign(name: Token, value: Any?) {
        when {
            values.containsKey(name.lexeme) -> values[name.lexeme] = value
            enclosing != null -> enclosing.assign(name, value)
            else -> throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }
    }

    fun define(name: String, value: Any?) {
        values[name] = value
    }
}