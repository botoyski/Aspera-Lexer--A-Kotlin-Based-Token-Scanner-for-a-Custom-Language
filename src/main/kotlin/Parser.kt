// Parser.kt
class Parser(private val tokens: List<Token>) {

    private var current = 0

    // UNIQUE-D: soft panic flag used during error reporting and recovery
    private var panicMode = false

    // Entry point: parse a single expression
    fun parse(): Expr {
        val expr = expression()
        // If there are extra non-EOF tokens, report once (optional)
        if (!isAtEnd()) {
            error(peek(), "Unexpected token after expression.")
        }
        return expr
    }

    // expression → equality
    private fun expression(): Expr = equality()

    // equality → comparison ( ( "!=" | "==" ) comparison )*
    private fun equality(): Expr {
        var expr = comparison()
        // UNIQUE-A: using accept(...) as a chain matcher instead of textbook match()
        while (accept(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val op = previous()
            val right = comparison()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }

    // comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )*
    private fun comparison(): Expr {
        var expr = term()
        while (accept(
                TokenType.GREATER, TokenType.GREATER_EQUAL,
                TokenType.LESS, TokenType.LESS_EQUAL
            )) {
            val op = previous()
            val right = term()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }

    // term → factor ( ( "-" | "+" ) factor )*
    // UNIQUE-C used here: if multiple + or - occur, produce a Chain node (flattened)
    private fun term(): Expr {
        val elements = mutableListOf<Expr>()
        elements.add(factor())

        val opsSeen = mutableListOf<Token>() // keep operators seen (should be same operator family)
        while (accept(TokenType.MINUS, TokenType.PLUS)) {
            opsSeen.add(previous())
            elements.add(factor())
        }

        return if (elements.size > 1) {
            // choose the last operator token as representative (operators are same precedence group)
            // This preserves operator text (either + or -). In mixed + and - expressions,
            // we still flatten; semantic evaluation (if you implement evaluator) can treat it left-to-right.
            Expr.Chain(opsSeen.last(), elements)
        } else {
            elements.first()
        }
    }

    // factor → unary ( ( "/" | "*" ) unary )*
    // For multiplication/division we keep textbook Binary nodes (no chain flattening here),
    // but we still use our accept() matcher.
    private fun factor(): Expr {
        var expr = unary()
        while (accept(TokenType.SLASH, TokenType.STAR)) {
            val op = previous()
            val right = unary()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }

    // unary → ( "!" | "-" ) unary | primary
    private fun unary(): Expr {
        if (accept(TokenType.BANG, TokenType.MINUS)) {
            val op = previous()
            val right = unary()
            return Expr.Unary(op, right)
        }
        return primary()
    }

    // primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")"
    private fun primary(): Expr {
        if (accept(TokenType.NUMBER)) {
            val num = previous().value as? Double ?: previous().text.toDouble()
            return Expr.Literal(num)
        }
        if (accept(TokenType.STRING)) {
            return Expr.Literal(previous().value as? String ?: "")
        }
        if (accept(TokenType.TRUE))  return Expr.Literal(true)
        if (accept(TokenType.FALSE)) return Expr.Literal(false)
        if (accept(TokenType.NIL))   return Expr.Literal(null)

        if (accept(TokenType.IDENTIFIER)) {
            val name = previous().text
            return Expr.Literal(name) // NEW: Implicit string
        }

        if (accept(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }

        error(peek(), "Expect expression.")
        return Expr.Literal(null)
    }


    // ---------------- helpers ----------------

    // UNIQUE-A: accept acts like a pattern matcher: if next token is any of the given types, consume it and return true.
    // This reads as "accept these tokens" (a stylistic DSL-like change).
    private fun accept(vararg types: TokenType): Boolean {
        for (t in types) {
            if (check(t)) { advance(); return true }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        error(peek(), message)
        // Synthesize a token to keep parsing
        return Token(type, "", null, if (isAtEnd()) previous().line else peek().line)
    }

    private fun check(type: TokenType): Boolean =
        !isAtEnd() && peek().type == type

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun peek(): Token = tokens[current]
    private fun previous(): Token = tokens[current - 1]

    // UNIQUE-D + UNIQUE-F: enhanced error handling with panic mode and synchronization.
    private fun error(token: Token, message: String) {
        if (panicMode) return
        panicMode = true
        if (token.type == TokenType.EOF) {
            System.err.println("[line ${token.line}] Error at end: $message")
        } else {
            System.err.println("[line ${token.line}] Error at '${token.text}': $message")
        }
        synchronize()
    }

    // UNIQUE-D: attempt to recover and continue parsing so the REPL reports more errors instead of stopping.
    private fun synchronize() {
        // consume the current token to move forward
        if (!isAtEnd()) advance()

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) {
                panicMode = false
                return
            }

            when (peek().type) {
                TokenType.VAR, TokenType.FOR, TokenType.IF, TokenType.WHILE,
                TokenType.PRINT, TokenType.RETURN -> {
                    panicMode = false
                    return
                }
                else -> {
                    // keep skipping tokens until a likely statement boundary
                }
            }
            advance()
        }
        // if we consumed everything, leave panicMode true to prevent duplicate messages
    }
}
