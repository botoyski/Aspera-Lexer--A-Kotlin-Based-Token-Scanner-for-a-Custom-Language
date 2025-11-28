sealed interface Stmt {

    // expression followed by semicolon → example:  1 + 2;
    data class Expression(val expr: Expr) : Stmt

    // print statement → print <expr>;
    data class Print(val expr: Expr) : Stmt

    // variable declaration → var x = value;
    data class Var(val name: Token, val initializer: Expr?) : Stmt

    // block → { ... }
    data class Block(val statements: List<Stmt>) : Stmt
}