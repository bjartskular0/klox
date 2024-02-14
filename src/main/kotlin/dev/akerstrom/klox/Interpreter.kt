package dev.akerstrom.klox

data object Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    val globals = Environment()
    private var environment = globals

    init {
        globals.define("clock", object: LoxCallable {
            override fun arity() = 0

            override fun call(interpreter: Interpreter, arguments: List<Any?>) = System.currentTimeMillis() / 1000.0

            override fun toString() = "<native fn>"
        })
    }

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            runtimeError(error)
        }
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }

            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }

            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }

            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }

            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) - (right as Double)
            }

            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) / (right as Double)
            }

            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) * (right as Double)
            }

            TokenType.PLUS -> {
                if (left is Double && right is Double) {
                    left + right
                } else if (left is String && right is String) {
                    left + right
                } else {
                    throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
                }
            }

            TokenType.BANG_EQUAL -> left != right
            TokenType.EQUAL_EQUAL -> left == right
            else -> null
        }
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)

        val arguments = mutableListOf<Any?>()
        for (argument in expr.arguments) {
            arguments.add(evaluate(argument))
        }

        if (callee !is LoxCallable) {
            throw RuntimeError(expr.paren, "Can only call functions and classes.")
        }

        if (arguments.size != callee.arity()) {
            throw RuntimeError(expr.paren, "Expected ${callee.arity()} arguments but got ${arguments.size}.")
        }

        return callee.call(this, arguments)
    }

    override fun visitGroupingExpr(expr: Expr.Grouping) = evaluate(expr.expression)

    override fun visitLiteralExpr(expr: Expr.Literal) = expr.value

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }

        return evaluate(expr.right)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.BANG -> !isTruthy(right)
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }

            else -> null
        }
    }

    override fun visitVariableExpr(expr: Expr.Variable) = environment.get(expr.name)

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun isTruthy(value: Any?): Boolean {
        return when (value) {
            null -> false
            is Boolean -> value
            else -> true
        }
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a == b
    }

    private fun stringify(value: Any?): String {
        return when (value) {
            null -> "nil"
            is Double -> {
                var text = value.toString()
                if (text.endsWith(".0")) {
                    text = text.substring(0, text.length - 2)
                }
                text
            }

            else -> value.toString()
        }
    }

    private fun evaluate(expr: Expr) = expr.accept(this)

    private fun execute(stmt: Stmt) = stmt.accept(this)

    fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment

            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }

    // Statements
    override fun visitBlockStmt(stmt: Stmt.Block) = executeBlock(stmt.statements, Environment(environment))

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(stmt, environment)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        throw Return(if (stmt.value != null) evaluate(stmt.value) else null)
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        var value: Any? = null
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }

        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }
}