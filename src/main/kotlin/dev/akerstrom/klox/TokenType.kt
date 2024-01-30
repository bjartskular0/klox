package dev.akerstrom.klox

enum class TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT,
    MINUS, PLUS,
    SEMICOLON,
    SLASH,
    STAR,

    // One or two character tokens.
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals.
    IDENTIFIER,
    STRING,
    NUMBER,

    // Keywords.
    CLASS, SUPER, THIS,
    VAR, FUN,
    AND, OR,
    IF, ELSE,
    TRUE, FALSE,
    FOR, WHILE,
    RETURN, NIL,
    PRINT,

    EOF
}