sealed interface StmtLang {
    data class Expression(val expr: Expr) : StmtLang
    data class Print(val expr: Expr) : StmtLang
    data class Var(val name: Token, val initializer: Expr?) : StmtLang
    data class Block(val statements: List<StmtLang>) : StmtLang
    data class If(val condition: Expr, val thenBranch: StmtLang, val elseBranch: StmtLang?) : StmtLang
    data class While(val condition: Expr, val body: StmtLang) : StmtLang
    data class Function(val name: Token, val params: List<Token>, val body: List<StmtLang>) : StmtLang
    data class Return(val keyword: Token, val value: Expr?) : StmtLang
    data class For(
        val initializer: StmtLang?,             // var i = 0;
        val condition: Expr?,                   // i < 10
        val increment: Expr?,                   // i = i + 1
        val body: StmtLang                      // { ... }
    ) : StmtLang
}