class Parser(private val tokens: List<Token>) {

    private var current = 0
    private var panicMode = false

    // Top-level: parse entire source into a Program with a chain of statements
    fun parse(): Stmt.Program {
        var first: Stmt? = null
        var last: Stmt? = null

        while (!isAtEnd()) {
            val stmt = declarationForChain()
            if (first == null) {
                first = stmt
                last = stmt
            } else if (last != null) {
                last = attachNext(last, stmt)
            }
        }
        return Stmt.Program(first)
    }

    // Build a single statement at top level
    private fun declarationForChain(): Stmt {
        return try {
            if (tatagos(TokenType.VAR)) varDeclaration(null) else statement(null)
        } catch (e: RuntimeException) {
            error(peek(), e.message ?: "Parse error")
            Stmt.Expression(Expr.Literal(null), null)
        }
    }

    // Attach 'next' pointer and return the updated tail (the node we just updated)
    private fun attachNext(prev: Stmt, next: Stmt): Stmt {
        return when (prev) {
            is Stmt.Expression -> prev.copy(next = next)
            is Stmt.Print -> prev.copy(next = next)
            is Stmt.Var -> prev.copy(next = next)
            is Stmt.Block -> prev.copy(next = next)
            is Stmt.Program -> prev
        }
    }

    // -------------- statements --------------

    // varDecl → "var" IDENTIFIER ( "=" expression )? ";"
    private fun varDeclaration(next: Stmt?): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        var initializer: Expr? = null
        if (tatagos(TokenType.EQUAL)) {
            initializer = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer, next)
    }

    // statement → print | block | exprStmt
    private fun statement(next: Stmt?): Stmt {
        return when {
            tatagos(TokenType.PRINT) -> printStatement(next)
            tatagos(TokenType.LEFT_BRACE) -> blockStatement(next)
            else -> expressionStatement(next)
        }
    }

    // printStmt → "print" expression ";"
    private fun printStatement(next: Stmt?): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(expr, next)
    }

    // block → "{" (declaration)* "}"
    private fun blockStatement(next: Stmt?): Stmt {
        var first: Stmt? = null
        var last: Stmt? = null

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            val stmt = declarationForChainInBlock()
            if (first == null) {
                first = stmt
                last = stmt
            } else if (last != null) {
                last = attachNext(last, stmt)
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        val innerProgram = Stmt.Program(first)
        return Stmt.Block(innerProgram, next)
    }

    // Version used inside blocks so we stop correctly at '}' or EOF
    private fun declarationForChainInBlock(): Stmt {
        return try {
            if (tatagos(TokenType.VAR)) varDeclaration(null) else statement(null)
        } catch (e: RuntimeException) {
            error(peek(), e.message ?: "Parse error")
            Stmt.Expression(Expr.Literal(null), null)
        }
    }

    private fun expressionStatement(next: Stmt?): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr, next)
    }

    // -------------- expressions --------------

    private fun expression(): Expr = assignment()

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

    private fun equality(): Expr {
        var expr = comparison()
        while (tatagos(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val op = previous()
            val right = comparison()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }

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

    private fun term(): Expr {
        var expr = factor()
        while (tatagos(TokenType.MINUS, TokenType.PLUS)) {
            val op = previous()
            val right = factor()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }

    private fun factor(): Expr {
        var expr = unary()
        while (tatagos(TokenType.SLASH, TokenType.STAR)) {
            val op = previous()
            val right = unary()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }

    private fun unary(): Expr {
        if (tatagos(TokenType.BANG, TokenType.MINUS)) {
            val op = previous()
            val right = unary()
            return Expr.Unary(op, right)
        }
        return primary()
    }

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

    // -------------- helpers --------------

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
