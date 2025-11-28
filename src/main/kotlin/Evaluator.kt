class Evaluator {

    // 🔥 LAB 4 ADDITION: Global environment storage for variables + scopes
    private var environment = Environment()

    // LAB 4: Evaluates *statements*, not only expressions
    fun execute(statements: List<Stmt>) {
        for (stmt in statements) execute(stmt)
    }

    // LAB 4: New statement types added
    private fun execute(stmt: Stmt) {
        when (stmt) {
            // LAB 4: Expression statements (result discarded)
            is Stmt.Expression -> evaluate(stmt.expr)

            // LAB 4: Print statement
            is Stmt.Print -> println(stringify(evaluate(stmt.expr)))

            // LAB 4: var declaration (supports initializer or nil)
            is Stmt.Var -> {
                val value = stmt.initializer?.let { evaluate(it) } ?: null
                environment.define(stmt.name.text, value)
            }

            // LAB 4: Block statements → new scope `{ ... }`
            is Stmt.Block -> executeBlock(stmt.statements, Environment(environment))
        }
    }

    // LAB 4: Scoping — local variables die after block ends
    fun executeBlock(statements: List<Stmt>, newEnv: Environment) {
        val previous = environment
        try {
            environment = newEnv      // enter new scope
            for (stmt in statements) execute(stmt)
        } finally {
            environment = previous    // exit scope
        }
    }

    // ───────────────────────────────────────────────
    // EXPRESSION EVALUATION  (THIS WAS LAB 3)
    // ───────────────────────────────────────────────

    fun evaluate(expr: Expr): Any? = when(expr) {

        // LAB 3
        is Expr.Literal -> expr.value
        is Expr.Grouping -> evaluate(expr.expression)

        // LAB 4: variable lookup
        is Expr.Identifier -> environment.get(expr.name)

        // LAB 4: assignment expression `x = value`
        is Expr.Assign -> {
            val value = evaluate(expr.value)
            environment.assign(expr.name, value)
            value
        }

        // LAB 3
        is Expr.Unary -> {
            val right = evaluate(expr.right)
            when (expr.operator.type) {
                TokenType.MINUS -> -(right as Double)
                TokenType.BANG -> !isTruthy(right)
                else -> null
            }
        }

        // LAB 3
        is Expr.Binary -> {
            val left = evaluate(expr.left)
            val right = evaluate(expr.right)
            when (expr.operator.type) {

                TokenType.PLUS  -> numberOrStringPlus(left,right)
                TokenType.MINUS -> (left as Double) - (right as Double)
                TokenType.STAR  -> (left as Double) * (right as Double)
                TokenType.SLASH -> {
                    if ((right as Double)==0.0) error("Division by zero")
                    (left as Double) / right
                }

                TokenType.GREATER -> (left as Double) > (right as Double)
                TokenType.LESS    -> (left as Double) < (right as Double)

                // 🔥 LAB 4 + LAB 3 behavior combined: logical equality
                TokenType.EQUAL_EQUAL -> left == right
                TokenType.BANG_EQUAL  -> left != right

                else -> null
            }
        }
    }

    // LAB 3 (utility functions reused in LAB 4)
    private fun stringify(v: Any?) = v ?: "nil"
    private fun isTruthy(v: Any?) = v != null && v != false

    private fun numberOrStringPlus(a: Any?, b: Any?) =
        if (a is Double && b is Double) a + b else "${a}${b}"
}
