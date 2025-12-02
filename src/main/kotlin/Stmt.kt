sealed interface Stmt {
    data class Program(val character: Character) : Stmt

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
}

