// Expr.kt — simplified, no Chain flattening
sealed interface Expr {
    // LITERALS (already in LAB 2 & 3)
    data class Literal(val value: Any?) : Expr
    data class Unary(val operator: Token, val right: Expr) : Expr
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr
    data class Grouping(val expression: Expr) : Expr

    // LAB 4 — variable name access
    data class Identifier(val name: Token) : Expr

    // LAB 4 — assignment (`x = value`)
    data class Assign(val name: Token, val value: Expr) : Expr
}
