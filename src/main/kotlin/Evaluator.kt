class Evaluator {
    fun execute(program: Stmt.Program) {
        if (program.characters.isEmpty()) {
            println("No characters defined.")
            return
        }

        // Print all character sheets
        for ((idx, c) in program.characters.withIndex()) {
            println("=== Character #${idx + 1} Sheet ===")
            printCharacterSheet(c)
            println()
        }

        // If at least two characters, make them fight
        if (program.characters.size >= 2) {
            println("=== Battle Demo ===")
            val a = program.characters[0]
            val b = program.characters[1]
            val winner = battle(a, b)
            println("=== Battle Over ===")
            println("Winner: ${nameOf(winner)}")
        }
    }

    private fun printCharacterSheet(c: Stmt.Character) {
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

    // ===== Battle system (while + recursion) =====

    private fun nameOf(c: Stmt.Character): String =
        "${c.race} ${c.clazz ?: ""}".trim()

    fun battle(a: Stmt.Character, b: Stmt.Character): Stmt.Character {
        var hpA = hitPoints(a)
        var hpB = hitPoints(b)

        println("${nameOf(a)} vs ${nameOf(b)}")
        println("${nameOf(a)} HP = $hpA, ${nameOf(b)} HP = $hpB")

        var round = 1

        // while loop: continues until one reaches 0 HP
        while (hpA > 0 && hpB > 0) {
            println("--- Round $round ---")

            // A attacks B
            val dmgAB = if (isWizardFireUser(a) && shouldUseFlurry()) {
                println("${nameOf(a)} prepares a fiery flurry!")
                fireFlurryDamage(a, b, 2)  // recursive INT-based fire attack
            } else {
                computePhysicalDamage(a, b)
            }
            hpB -= dmgAB
            println("${nameOf(a)} hits ${nameOf(b)} for $dmgAB (HP = ${hpB.coerceAtLeast(0)})")
            if (hpB <= 0) break

            // B attacks A
            val dmgBA = if (isWizardFireUser(b) && shouldUseFlurry()) {
                println("${nameOf(b)} prepares a fiery flurry!")
                fireFlurryDamage(b, a, 2)
            } else {
                computePhysicalDamage(b, a)
            }
            hpA -= dmgBA
            println("${nameOf(b)} hits ${nameOf(a)} for $dmgBA (HP = ${hpA.coerceAtLeast(0)})")

            round++
        }

        return if (hpA > 0) a else b
    }

    private fun isWizardFireUser(c: Stmt.Character): Boolean {
        val clazz = c.clazz ?: ""
        return clazz.equals("Sorcerer", ignoreCase = true) ||
                (clazz.equals("Mage", ignoreCase = true) && c.magicAffinity == "Fire")
    }

    private fun shouldUseFlurry(): Boolean {
        // 30% chance to use flurry; tweak as needed
        val roll = (0..99).random()
        return roll < 30
    }

    private fun computePhysicalDamage(attacker: Stmt.Character, defender: Stmt.Character): Int {
        val base = (attacker.attributes["STR"] ?: 5)
        val ac = armorClass(defender)
        val raw = base + (0..2).random()
        val reduced = raw - (ac / 5)
        return reduced.coerceAtLeast(1)
    }

    // Recursive fire flurry: multiple INT-based hits
    private fun fireFlurryDamage(
        attacker: Stmt.Character,
        defender: Stmt.Character,
        hitsLeft: Int
    ): Int {
        if (hitsLeft <= 0) return 0

        val intStat = attacker.attributes["INT"] ?: 5
        val magicBonus = if (attacker.magicAffinity == "Fire") 3 else 0
        val base = intStat + magicBonus
        val ac = armorClass(defender)
        val raw = base + (0..3).random()   // slightly more swingy
        val reduced = raw - (ac / 6)       // armor matters less vs magic
        val oneHit = reduced.coerceAtLeast(1)

        return oneHit + fireFlurryDamage(attacker, defender, hitsLeft - 1)
    }
}

// ===== helpers =====

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
