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
        println("Skills: ${c.skills.joinToString(", ")}")
        println("Equipment:")
        println(" Weapon: ${c.weapon}")
        println(" Armor: ${c.armor}")
        println(" Accessory: ${c.accessory}")
        println("Alignment: ${c.alignment}")
        println("Magic Affinity: ${c.magicAffinity}")
    }
}