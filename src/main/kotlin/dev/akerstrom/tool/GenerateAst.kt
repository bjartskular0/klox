package dev.akerstrom.tool

/***
 * This tools original use is to generate java boilerplate code.
 * In kotlin however, the resulting code is about as big as the
 * original AST representation.
 * Thus, this tool is quite useless in its *current* state.
 */

import java.io.File
import java.io.PrintWriter
import kotlin.system.exitProcess

val indent = " ".repeat(4)

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: generate_ast <output directory>")
        exitProcess(64)
    }

    val outputDir = args[0]

    defineAst(
        outputDir, "Expr", listOf(
            "Assign   : val name: Token, val value: Expr",
            "Binary   : val left: Expr, val operator: Token, val right: Expr",
            "Call     : val callee: Expr, val paren: Token, val arguments: List<Expr>",
            "Grouping : val expression: Expr",
            "Literal  : val value: Any?",
            "Logical  : val left: Expr, val operator: Token, val right: Expr",
            "Unary    : val operator: Token, val right: Expr",
            "Variable : val name: Token"
        )
    )

    defineAst(
        outputDir, "Stmt", listOf(
            "Block      : val statements: List<Stmt>",
            "Expression : val expression: Expr",
            "Function   : val name: Token, val params: List<Token>, val body: List<Stmt>",
            "If         : val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?",
            "Print      : val expression: Expr",
            "Return     : val keyword: Token, val value: Expr?",
            "Var        : val name: Token, val initializer: Expr?",
            "While      : val condition: Expr, val body: Stmt"
        )
    )
}

fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = "$outputDir/$baseName.kt"
    File(path).printWriter().use { out ->
        out.println("package dev.akerstrom.klox")
        out.println()
        out.println("sealed class $baseName {")
        out.println("${indent}abstract fun <R> accept(visitor: Visitor<R>): R")
        out.println()
        defineVisitor(out, baseName, types)
        out.println()

        // The AST classes.
        for (type in types) {
            // Only split on first occurrence of ':'
            val (className, fields) = type.split(":", limit = 2).map { it.trim() }
            defineType(out, baseName, className, fields)
        }

        out.println("}")
        out.println()
    }
}

fun defineVisitor(out: PrintWriter, baseName: String, types: List<String>) {
    out.println("${indent}sealed interface Visitor<R> {")
    for (type in types) {
        val typeName = type.split(":")[0].trim()
        out.println("${indent.repeat(2)}fun visit$typeName$baseName(${baseName.lowercase()}: $typeName): R")
    }
    out.println("${indent}}")
}

fun defineType(out: PrintWriter, baseName: String, className: String, fieldList: String) {
    out.println("${indent}data class $className($fieldList) : $baseName() {")
    out.println("${indent.repeat(2)}override fun <R> accept(visitor: Visitor<R>) = visitor.visit$className$baseName(this)")
    out.println("${indent}}")
}