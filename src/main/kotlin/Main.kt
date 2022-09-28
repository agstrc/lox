package main

import lox.AstPrinter
import lox.Parser
import lox.Scanner
import lox.Token
import java.io.File
import kotlin.system.exitProcess

private var hadError = false

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: lox [script]")
        exitProcess(64)
    } else if (args.size == 1) {
        runFile(args[0])
    } else {
        runPrompt()
    }
}

fun runFile(path: String) {
    runSource(File(path).readText())
    if (hadError) exitProcess(65)
}

fun runPrompt() {
    while (true) {
        print("> ")
        val line = readLine() ?: break
        runSource(line)
        hadError = false
    }
}

fun runSource(source: String) {
    val tokens = Scanner(source).scanTokens()
    val expression = Parser(tokens).parse()

    if (hadError) return
    // if expression is null, hadError must be true. Therefore, it is safe to assume
    // expression is not null here.
    println(AstPrinter().print(expression!!))
}

fun error(line: Int, msg: String) {
    report(line, "", msg)
}


fun report(line: Int, where: String, message: String) {
    System.err.println("[line $line] Error$where: $message")
    hadError = true
}


fun error(token: Token, message: String) {
    if (token.type == Token.Type.EOF) {
        report(token.line, " at end", message)
    } else {
        report(token.line, " at '${token.lexeme}'", message)
    }
}