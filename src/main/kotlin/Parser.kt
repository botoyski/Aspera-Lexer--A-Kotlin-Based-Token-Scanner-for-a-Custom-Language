// Parser.kt – parses to a list of Stmt (Lab 4 style)

class Parser(private val tokens: List<Token>) {

    private var current = 0
    private var panicMode = false

    // Entry point: parse a whole program (list of statements)
    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            statements += declaration()
        }
        return statements
    }

    // declaration → "var" varDecl | statement
    private fun declaration(): Stmt {
        return try {
            if (tatagos(TokenType.VAR)) varDeclaration() else statement()
        } catch (e: RuntimeException) {
            error(peek(), e.message ?: "Parse error")
            Stmt.Expression(Expr.Literal(null))
        }
    }

    // varDecl → IDENTIFIER ( "=" expression )? ";"
    private fun varDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        var initializer: Expr? = null
        if (tatagos(TokenType.EQUAL)) {
            initializer = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    // statement → printStmt | block | exprStmt
    private fun statement(): Stmt {
        return when {
            tatagos(TokenType.PRINT) -> printStatement()
            tatagos(TokenType.LEFT_BRACE) -> Stmt.Block(block())
            else -> expressionStatement()
        }
    }

    // printStmt → "print" expression ";"
    private fun printStatement(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(expr)
    }

    // block → "{" declaration* "}"
    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements += declaration()
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    // exprStmt → expression ";"
    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    // ---------------- EXPRESSIONS ----------------

    // expression → assignment
    private fun expression(): Expr = assignment()

    // assignment → IDENTIFIER "=" assignment | equality
    private fun assignment(): Expr {
        var expr = equality()

        if (tatagos(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Identifier) {
                return Expr.Assign(expr.name, value)
            }

            throw RuntimeException("Invalid assignment target at '${equals.text}'.")
        }

        return expr
    }

    // equality → comparison ( ( "!=" | "==" ) comparison )*
    private fun equality(): Expr {
        var expr = comparison()
        while (tatagos(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val op = previous()
            val right = comparison()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }

    // comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )*
    private fun comparison(): Expr {
        var expr = term()
        while (tatagos(
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
    private fun term(): Expr {
        var expr = factor()
        while (tatagos(TokenType.MINUS, TokenType.PLUS)) {
            val op = previous()
            val right = factor()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }

    // factor → unary ( ( "/" | "*" ) unary )*
    private fun factor(): Expr {
        var expr = unary()
        while (tatagos(TokenType.SLASH, TokenType.STAR)) {
            val op = previous()
            val right = unary()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }

    // unary → ( "!" | "-" ) unary | primary
    private fun unary(): Expr {
        if (tatagos(TokenType.BANG, TokenType.MINUS)) {
            val op = previous()
            val right = unary()
            return Expr.Unary(op, right)
        }
        return primary()
    }

    // primary → NUMBER | STRING | "true" | "false" | "nil" | IDENTIFIER | "(" expression ")"
    private fun primary(): Expr {
        if (tatagos(TokenType.NUMBER)) {
            val num = previous().value as Double
            return Expr.Literal(num)
        }
        if (tatagos(TokenType.STRING)) {
            return Expr.Literal(previous().value as String)
        }
        if (tatagos(TokenType.TRUE)) return Expr.Literal(true)
        if (tatagos(TokenType.FALSE)) return Expr.Literal(false)
        if (tatagos(TokenType.NIL)) return Expr.Literal(null)

        if (tatagos(TokenType.IDENTIFIER)) {
            val name = previous()
            return Expr.Identifier(name)
        }

        if (tatagos(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }

        error(peek(), "Expect expression.")
        return Expr.Literal(null)
    }

    // --------------- helpers ----------------

    private fun tatagos(vararg types: TokenType): Boolean {
        for (t in types) {
            if (check(t)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        error(peek(), message)
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

    private fun error(token: Token, message: String) {
        if (panicMode) return
        panicMode = true
        // for lab: no printing, just sync
        synchronize()
    }

    private fun synchronize() {
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
                else -> advance()
            }
        }
    }
}
