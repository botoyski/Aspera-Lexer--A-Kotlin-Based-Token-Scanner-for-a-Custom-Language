fun main(args: Array<String>) {
    try {
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
    } catch (e: RuntimeException) {
        println(e.message)
    }
}

fun run(source: String) {
    // Normalize line endings so split works on Windows and Unix
    val normalized = source.replace("\r\n", "\n").replace("\r", "\n")

    // character + optional script section
    val splitIndex = normalized.indexOf("\nscript:", ignoreCase = true)
    val (charSource, scriptSource) =
        if (splitIndex == -1) {
            normalized to ""
        } else {
            val before = normalized.substring(0, splitIndex)
            // Find end of "script:" line
            val lineEnd = normalized.indexOf('\n', splitIndex + 1)
                .let { if (it == -1) normalized.length else it + 1 }
            val after = normalized.substring(lineEnd)
            before to after
        }

    // charSource only
    val charScanner = Scanner(charSource)
    val charTokens = charScanner.scanTokens()

    // Check if there is at least one CHARACTER_KW before EOF
    val hasCharacterSection = charTokens.any { it.type == TokenType.CHARACTER_KW }

    if (hasCharacterSection) {
        val charParser = Parser(charTokens)
        val program = charParser.parse()
        val evaluator = Evaluator()
        evaluator.execute(program)
    }

    // script section
    if (scriptSource.isNotBlank()) {
        val scriptScanner = Scanner(scriptSource)
        val scriptTokens = scriptScanner.scanTokens()

        // for (t in scriptTokens) {
        //     println("${t.line}: ${t.type} '${t.text}'")
        // }

        val scriptParser = Parser(scriptTokens)
        val scriptStmts = scriptParser.parseScriptProgram()
        val scriptInterp = Interpreter()
        scriptInterp.interpret(scriptStmts)
    }
}
