package dev.akerstrom.klox

class LoxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment
) : LoxCallable {
    override fun arity() = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        for ((i, v) in declaration.params.withIndex()) {
            environment.define(v.lexeme, arguments[i])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return null
    }

    override fun toString() = "<fn ${declaration.name.lexeme}>"
}