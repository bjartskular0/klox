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

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: generate_ast <output directory>")
        exitProcess(64)
    }

    val outputDir = args[0]

    defineAst(outputDir, "Expr", listOf(
        "Binary   : val left: Expr, val operator: Token, val right: Expr",
        "Grouping : val expression: Expr",
        "Literal  : val value: Any?",
        "Unary    : val operator: Token, val right: Expr"
    ))
}

fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = "$outputDir/$baseName.kt"
    File(path).printWriter().use { out ->
        out.println("package dev.akerstrom.klox")
        out.println()
        out.println("sealed class $baseName")
        out.println()

        // The AST classes.
        for (type in types) {

            val sp = type.split(":", limit = 2)
            val (className, fields) = sp.map { it.trim() }
            out.println("data class $className($fields) : $baseName()")
        }
    }
}

fun defineType(out: PrintWriter, baseName: String, className: String, fieldList: String) {
    out.println("data class $className($fieldList) : $baseName()")
}