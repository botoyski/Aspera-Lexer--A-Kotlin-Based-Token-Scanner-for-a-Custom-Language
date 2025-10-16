// keyword table: reserved words mapped to their token types
val keywords = mapOf(
    "var" to TokenType.VAR,
    "if" to TokenType.IF,
    "else" to TokenType.ELSE,
    "true" to TokenType.TRUE,
    "false" to TokenType.FALSE,
    "nil" to TokenType.NIL,
    "for" to TokenType.FOR,
    "while" to TokenType.WHILE,
    "print" to TokenType.PRINT,
    "return" to TokenType.RETURN,
    "val" to TokenType.VAL
)