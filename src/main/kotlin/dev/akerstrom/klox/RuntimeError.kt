package dev.akerstrom.klox

class RuntimeError(val token: Token, message: String) : RuntimeException(message)