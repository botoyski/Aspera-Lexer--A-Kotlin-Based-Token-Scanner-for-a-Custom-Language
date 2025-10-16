// Expr.kt
sealed interface Expr {
    data class Literal(val value: Any?) : Expr
    data class Unary(val operator: Token, val right: Expr) : Expr
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr

    // UNIQUE-C: Chain node - a flattened associative operator node.
    // This collects many operands for the same operator instead of nesting Binary nodes.
    data class Chain(val operator: Token, val elements: List<Expr>) : Expr

    data class Grouping(val expression: Expr) : Expr
}
