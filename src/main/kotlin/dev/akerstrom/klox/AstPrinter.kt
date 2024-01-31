package dev.akerstrom.klox

import kotlin.math.exp

class AstPrinter : Visitor<String> {
    fun print(expr: Expr) = expr.accept(this)

    override fun visitBinaryExpr(expr: Binary) = parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visitGroupingExpr(expr: Grouping) = parenthesize("group", expr.expression)

    override fun visitLiteralExpr(expr: Literal) = expr.value?.toString() ?: "nil"

    override fun visitUnaryExpr(expr: Unary) = parenthesize(expr.operator.lexeme, expr.right)

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        return "($name ${exprs.joinToString(" ") { it.accept(this) }})"
    }
}

fun main() {
    val expression = Binary(
        Unary(
            Token(TokenType.MINUS, "-", null, 1),
            Literal(123)
        ),
        Token(TokenType.STAR, "*", null, 1),
        Grouping(Literal(45.67))
    )

    println(AstPrinter().print(expression))
}