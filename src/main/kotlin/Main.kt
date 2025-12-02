fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        val source = java.io.File(args[0]).readText()
        run(source)
        return
    }

    println("Enter a character description, end with an empty line:")
    val builder = StringBuilder()
    while (true) {
        val line = readLine() ?: break
        if (line.isBlank()) break
        builder.appendLine(line)
    }

    run(builder.toString())
}

fun run(source: String) {
    println("DEBUG: running file...")
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    // DEBUG: print tokens
    for (t in tokens) {
        println("${t.line}: ${t.type} '${t.text}'")
    }

    val parser = Parser(tokens)
    val program = parser.parse()
    val evaluator = Evaluator()
    evaluator.execute(program)
}

