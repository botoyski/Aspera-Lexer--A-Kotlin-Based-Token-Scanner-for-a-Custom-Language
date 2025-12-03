sealed interface Stmt {
    data class Program(val characters: List<Character>) : Stmt

    data class Character(
        val race: String,
        val clazz: String?,
        val background: String?,
        val attributes: Map<String, Int>,
        val skills: List<String>,
        val weapon: String,
        val armor: String,
        val accessory: String,
        val alignment: String,
        val magicAffinity: String
    ) : Stmt

    data class Expression(val expression: Expr) : Stmt
    data class Print(val expression: Expr) : Stmt
    data class Block(val statements: List<Stmt>) : Stmt
    data class If(
        val condition: Expr,
        val thenBranch: Stmt,
        val elseBranch: Stmt?
    ) : Stmt
    data class While(
        val condition: Expr,
        val body: Stmt
    ) : Stmt
}
