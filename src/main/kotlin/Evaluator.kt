class Evaluator {
    fun execute(program: Stmt.Program) {
        val c = program.character
        println("=== Character Sheet ===")
        println("Race: ${c.race}")
        if (c.clazz != null) println("Class: ${c.clazz}")
        if (c.background != null) println("Background: ${c.background}")
        println("Attributes:")
        for ((k, v) in c.attributes) {
            println(" $k = $v")
        }
        val problems = validateAttributes(c)
        if (problems.isNotEmpty()) {
            println("Validation problems:")
            problems.forEach { println("  - $it") }
        }
        println("Skills: ${c.skills.joinToString(", ")}")
        println("Equipment:")
        println(" Weapon: ${c.weapon}")
        println(" Armor: ${c.armor}")
        println(" Accessory: ${c.accessory}")
        println("Alignment: ${c.alignment}")
        println("Magic Affinity: ${c.magicAffinity}")
        println("Power Score: ${powerScore(c)}")
        println("Hook: ${storyHook(c)}")
        val tags = mutableListOf<String>()
        if (isTank(c)) tags.add("Tank")
        if (hasRangedFocus(c)) tags.add("Ranged")

        if (tags.isNotEmpty()) {
            println("Roles: ${tags.joinToString(", ")}")
        }


    }
}

fun armorClass(c: Stmt.Character): Int =
    10 + (c.attributes["DEX"] ?: 0) / 2

fun hitPoints(c: Stmt.Character): Int =
    (c.attributes["END"] ?: 0) * 2

fun carryCapacity(c: Stmt.Character): Int =
    (c.attributes["STR"] ?: 0) * 5

fun powerScore(c: Stmt.Character): Int {
    val sumAttrs = c.attributes.values.sum()
    val skillBonus = c.skills.size * 2
    val magicBonus = if (c.magicAffinity != "None") 5 else 0
    return sumAttrs + skillBonus + magicBonus

}

fun validateAttributes(c: Stmt.Character): List<String> {
    val errors = mutableListOf<String>()
    val total = c.attributes.values.sum()
    if (total > 80) errors.add("Total attribute points must be ≤ 80.")
    for ((name, v) in c.attributes) {
        if (v !in 1..20) errors.add("$name must be between 1 and 20.")
    }
    return errors
}

fun isTank(c: Stmt.Character): Boolean =
    (c.attributes["END"] ?: 0) >= 16 && c.armor in listOf("Plate", "Chainmail")

fun hasRangedFocus(c: Stmt.Character): Boolean =
    "Archery" in c.skills || c.weapon in listOf("Bow", "Spear")

fun storyHook(c: Stmt.Character): String {
    val race = c.race
    val bg = c.background ?: "Wanderer"
    val align = c.alignment
    val clazz = c.clazz ?: "Adventurer"

    val role = when {
        isTank(c) && hasRangedFocus(c) -> "frontline skirmisher"
        isTank(c) -> "unyielding defender"
        hasRangedFocus(c) -> "deadly marksman"
        else -> "drifter"
    }

    val templates = listOf(
        "A $align $race $bg who wanders the land in search of a lost destiny.",
        "A $align $race $clazz serving as a $role in battle.",
        "A $align $race $bg trying to escape the shadows of the past.",
        "A $align $race $clazz seeking power, glory, and answers about their origin.",
        "A $align $race $bg bound to an ancient oath they barely understand."
    )

    return templates.random()
}




