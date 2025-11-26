class Evaluator {

    fun evaluate(expr: Expr): Any? = when (expr) {
        is Expr.Literal  -> expr.value
        is Expr.Grouping -> evaluate(expr.expression)

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

                // arithmetic ---------------------------------------------------------
                TokenType.PLUS -> {
                    when {
                        left is Double && right is Double -> left + right
                        left is Double && right is Int -> left + right.toDouble()
                        left is Int && right is Double -> left.toDouble() + right
                        left is Int && right is Int -> left + right

                        // allow string concatenation
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

                // comparisons --------------------------------------------------------
                TokenType.GREATER -> numBool(expr.operator, left, right) { a, b -> a > b }
                TokenType.LESS    -> numBool(expr.operator, left, right) { a, b -> a < b }
                TokenType.GREATER_EQUAL -> numBool(expr.operator, left, right) { a, b -> a >= b }
                TokenType.LESS_EQUAL    -> numBool(expr.operator, left, right) { a, b -> a <= b }

                // equality -----------------------------------------------------------
                TokenType.EQUAL_EQUAL -> isEqual(left, right)
                TokenType.BANG_EQUAL  -> !isEqual(left, right)

                else -> runtimeError(expr.operator, "Unknown binary operator.")
            }
        }
    }

    // helpers --------------------------------------------------------------------

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

    // numeric operator helper
    private fun numOp(op: Token, a: Any?, b: Any?, fn: (Double, Double) -> Double): Double {
        if (a !is Number || b !is Number)
            runtimeError(op, "Operands must be numbers.")
        return fn(a.toDouble(), b.toDouble())
    }

    // comparison operator helper
    private fun numBool(op: Token, a: Any?, b: Any?, fn: (Double, Double) -> Boolean): Boolean {
        if (a !is Number || b !is Number)
            runtimeError(op, "Operands must be numbers.")
        return fn(a.toDouble(), b.toDouble())
    }

    private fun runtimeError(token: Token, message: String): Nothing {
        throw RuntimeException("[line ${token.line}] Runtime error: $message")
    }
}
