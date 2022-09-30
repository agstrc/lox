package lox

import lox.Token.Type.*

class Parser(private val tokens: List<Token>) {
    private class ParseError : RuntimeException()

    private var current = 0

    fun parse(): List<Stmt> {
        val statements: MutableList<Stmt> = ArrayList()
        while (!isAtEnd())
            statements.add(statement())

        return statements
    }

    private fun ruleMatcher(rule: () -> Expr, vararg types: Token.Type): () -> Expr {
        return fun(): Expr {
            var expr = rule()

            while (match(*types)) {
                val operator = previous()
                val right = rule()
                expr = Expr.Binary(expr, operator, right)
            }

            return expr
        }
    }

    private fun expression(): Expr = equality()

    private fun statement(): Stmt {
        if (match(PRINT)) return printStatement()

        return expressionStatement()
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    private val equality = ruleMatcher({ comparison() }, BANG_EQUAL, EQUAL_EQUAL)

    private val comparison = ruleMatcher({ term() }, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)

    private val term = ruleMatcher({ factor() }, MINUS, PLUS)

    private val factor = ruleMatcher({ unary() }, SLASH, STAR)

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return primary()
    }

    private fun primary(): Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NIL)) return Expr.Literal(null)

        if (match(NUMBER, STRING)) return Expr.Literal(previous().literal)

        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expected expression")
    }

    private fun match(vararg types: Token.Type): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun consume(type: Token.Type, message: String): Token {
        if (check(type)) return advance()

        throw error(peek(), message)
    }

    private fun check(type: Token.Type): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type === EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun error(token: Token, message: String): ParseError {
        main.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return

            when (peek().type) {
                CLASS,
                FUN,
                VAR,
                FOR,
                IF,
                WHILE,
                PRINT,
                RETURN -> {
                    return
                }

                else -> {/* do nothing */
                }
            }
        }
    }
}