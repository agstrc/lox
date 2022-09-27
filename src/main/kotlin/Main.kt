import lox.Scanner
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

    for (token in tokens) {
        println(token)
    }
}

fun error(line: Int, msg: String) {
    report(line, "", msg)
}


fun report(line: Int, where: String, message: String) {
    System.err.println("[line $line] Error$where: $message")
    hadError = true
}
