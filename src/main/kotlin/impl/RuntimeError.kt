package lox.impl

class RuntimeError(val token: Token, message: String) : RuntimeException(message)
