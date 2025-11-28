class Evaluator {

    // global / current environment
    private var environment = Environment()

    // ENTRY: run a whole program
    fun execute(statements: List<Stmt>) {
        for (stmt in statements) {
            execute(stmt)
        }
    }

    // run a single statement
    private fun execute(stmt: Stmt) {
        when (stmt) {
            is Stmt.Expression -> evaluate(stmt.expr)          // just evaluate, ignore result
            is Stmt.Print -> println(stringify(evaluate(stmt.expr)))
            is Stmt.Var -> {
                val value = stmt.initializer?.let { evaluate(it) }
                environment.define(stmt.name.text, value)
            }
            is Stmt.Block -> executeBlock(stmt.statements, Environment(environment))
        }
    }

    // enter a new block scope
    fun executeBlock(statements: List<Stmt>, newEnv: Environment) {
        val previous = environment
        try {
            environment = newEnv
            for (stmt in statements) execute(stmt)
        } finally {
            environment = previous
        }
    }

    // ---------- EXPRESSIONS (Lab 3 + Lab 4) ----------

    fun evaluate(expr: Expr): Any? = when (expr) {

        is Expr.Literal -> expr.value
        is Expr.Grouping -> evaluate(expr.expression)

        is Expr.Identifier -> environment.get(expr.name)

        is Expr.Assign -> {
            val value = evaluate(expr.value)
            environment.assign(expr.name, value)
            value
        }

        is Expr.Unary -> {
            val right = evaluate(expr.right)
            when (expr.operator.type) {
                TokenType.MINUS -> {
                    if (right !is Number) runtimeError(expr.operator, "Operand must be a number.")
                    -(right.toDouble())
                }
                TokenType.BANG -> !isTruthy(right)
                else -> runtimeError(expr.operator, "Unknown unary operator.")
            }
        }

        is Expr.Binary -> {
            val left = evaluate(expr.left)
            val right = evaluate(expr.right)
            when (expr.operator.type) {

                TokenType.PLUS -> {
                    when {
                        left is Double && right is Double -> left + right
                        left is Double && right is Int -> left + right.toDouble()
                        left is Int && right is Double -> left.toDouble() + right
                        left is Int && right is Int -> left + right
                        left is String || right is String -> left.toString() + right.toString()
                        else -> runtimeError(expr.operator, "Operands must be two numbers or two strings.")
                    }
                }

                TokenType.MINUS -> numOp(expr.operator, left, right) { a, b -> a - b }
                TokenType.STAR  -> numOp(expr.operator, left, right) { a, b -> a * b }
                TokenType.SLASH -> {
                    if (right == 0.0) runtimeError(expr.operator, "Division by zero.")
                    numOp(expr.operator, left, right) { a, b -> a / b }
                }

                TokenType.GREATER -> numBool(expr.operator, left, right) { a, b -> a > b }
                TokenType.LESS    -> numBool(expr.operator, left, right) { a, b -> a < b }
                TokenType.GREATER_EQUAL -> numBool(expr.operator, left, right) { a, b -> a >= b }
                TokenType.LESS_EQUAL    -> numBool(expr.operator, left, right) { a, b -> a <= b }

                TokenType.EQUAL_EQUAL -> isEqual(left, right)
                TokenType.BANG_EQUAL  -> !isEqual(left, right)

                else -> runtimeError(expr.operator, "Unknown binary operator.")
            }
        }
    }

    // ---------- helpers (same as you had) ----------

    private fun isTruthy(v: Any?): Boolean {
        if (v == null) return false
        if (v is Boolean) return v
        return true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        return when {
            a == null && b == null -> true
            a == null -> false
            else -> a == b
        }
    }

    private fun numOp(op: Token, a: Any?, b: Any?, fn: (Double, Double) -> Double): Double {
        if (a !is Number || b !is Number)
            runtimeError(op, "Operands must be numbers.")
        return fn(a.toDouble(), b.toDouble())
    }

    private fun numBool(op: Token, a: Any?, b: Any?, fn: (Double, Double) -> Boolean): Boolean {
        if (a !is Number || b !is Number)
            runtimeError(op, "Operands must be numbers.")
        return fn(a.toDouble(), b.toDouble())
    }

    private fun stringify(v: Any?) = v ?: "nil"

    private fun runtimeError(token: Token, message: String): Nothing {
        throw RuntimeException("[line ${token.line}] Runtime error: $message")
    }
}
