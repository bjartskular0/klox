package dev.akerstrom.klox

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.system.exitProcess

private val interpreter = Interpreter
var hadError = false
var hadRuntimeError = false

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: klox [script]")
        exitProcess(64)
    } else if (args.size == 1) {
        runFile(args[0])
    } else {
        runPrompt()
    }
}

fun runFile(path: String) {
    val text = File(path).readText()
    run(text)

    // Indicate an error in the exit code
    if (hadError) exitProcess(65)
    if (hadRuntimeError) exitProcess(70)
}

// REPL
fun runPrompt() {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)

    while (true) {
        print("> ")
        val line = reader.readLine() ?: break
        run(line)
        hadError = false
    }
}

fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    val parser = Parser(tokens)
    val statements = parser.parse()

    println("hadError: $hadError")

    // Stop if there was a syntax error.
    if (hadError) return

    // Bang-bang is fine because expression should only be null when hadError is true
    // println(AstPrinter().print(expression!!))

    interpreter.interpret(statements)
}

fun error(line: Int, message: String) {
    report(line, "", message)
}

fun report(line: Int, where: String, message: String) {
    println("[line $line] Error$where: $message")
    hadError = true
}

fun error(token: Token, message: String) {
    if (token.type == TokenType.EOF) {
        report(token.line, " at end", message)
    } else {
        report(token.line, " at '${token.lexeme}'", message)
    }
}

fun runtimeError(error: RuntimeError) {
    println("${error.message}\n[line ${error.token.line}]")
    hadRuntimeError = true
}