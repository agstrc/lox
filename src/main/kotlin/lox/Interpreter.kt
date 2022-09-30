package lox

import lox.Token.Type.*

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {


    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements)
                execute(statement)
        } catch (error: RuntimeError) {
            main.runtimeError(error)
        }
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }

            BANG -> !isTruthy(right)
            else -> null // unreachable
        }
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand !is Double) throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left !is Double || right !is Double) throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun isTruthy(value: Any?): Boolean {
        if (value === null) return false
        if (value is Boolean) return value
        return true
    }

    private fun isEqual(left: Any?, right: Any?): Boolean {
        return if (left == null && right == null) true else left?.equals(right) ?: false
    }

    private fun stringify(value: Any?): String {
        if (value == null) return "nil"

        if (value is Double) {
            var text = value.toString()
            if (text.endsWith(".0"))
                text = text.substring(0, text.length - 2)
            return text
        }

        return value.toString()
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }

            GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }

            LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }

            LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }

            MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) - (right as Double)
            }

            SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) / (right as Double)
            }

            STAR -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) * (right as Double)
            }

            PLUS -> if (left is Double && right is Double) left + right else
                if (left is String && right is String) left + right else
                    throw RuntimeError(expr.operator, "Operands must be two numbers or two strings")

            BANG_EQUAL -> !isEqual(left, right)
            EQUAL_EQUAL -> isEqual(left, right)
            else -> null
        }
    }
}