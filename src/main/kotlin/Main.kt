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
    // println("DEBUG: running file...")

    // Normalize line endings so split works on Windows and Unix
    val normalized = source.replace("\r\n", "\n").replace("\r", "\n")

    // 1. Split into character + optional script section
    val splitIndex = normalized.indexOf("\nscript:", ignoreCase = true)
    val (charSource, scriptSource) =
        if (splitIndex == -1) {
            normalized to ""
        } else {
            val before = normalized.substring(0, splitIndex)
            // Find end of "script:" line
            val lineEnd = normalized.indexOf('\n', splitIndex + 1).let { if (it == -1) normalized.length else it + 1 }
            val after = normalized.substring(lineEnd)
            before to after
        }

    // println("DEBUG: charSource =====")
    // println(charSource)
    // println("DEBUG: END charSource =====")

    // println("DEBUG: scriptSource =====")
    // println(scriptSource)
    // println("DEBUG: END scriptSource =====")

    // 2. Characters (medieval DSL) from charSource only
    val charScanner = Scanner(charSource)
    val charTokens = charScanner.scanTokens()

    // DEBUG: print tokens for characters
    // println("DEBUG: character tokens =====")
    // for (t in charTokens) {
    //     println("${t.line}: ${t.type} '${t.text}'")
    // }
    // println("DEBUG: END character tokens =====")

    val charParser = Parser(charTokens)
    val program = charParser.parse()
    val evaluator = Evaluator()
    evaluator.execute(program)

    // 3. Optional script section
    if (scriptSource.isNotBlank()) {
        val scriptScanner = Scanner(scriptSource)
        val scriptTokens = scriptScanner.scanTokens()

        // println("DEBUG: script tokens =====")
        // for (t in scriptTokens) {
        //     println("${t.line}: ${t.type} '${t.text}'")
        // }
        // println("DEBUG: END script tokens =====")

        val scriptParser = Parser(scriptTokens)
        val scriptStmts = scriptParser.parseScriptProgram()
        val scriptInterp = Interpreter()
        scriptInterp.interpret(scriptStmts)
    }
}