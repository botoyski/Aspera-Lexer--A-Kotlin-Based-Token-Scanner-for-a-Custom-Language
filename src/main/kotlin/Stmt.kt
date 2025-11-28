sealed interface Stmt {
    data class Program(val first: Stmt?) : Stmt
    data class Expression(val expr: Expr, val next: Stmt?) : Stmt
    data class Print(val expr: Expr, val next: Stmt?) : Stmt
    data class Var(val name: Token, val initializer: Expr?, val next: Stmt?) : Stmt
    data class Block(val body: Program, val next: Stmt?) : Stmt
}
