package com.github.barakb

import java.io.File
import kotlin.contracts.ExperimentalContracts

class NDSProcessor {

    private fun posOfFirst(input: String, from: Int?, ch: Char, levelParens: Boolean = false): Int? {
        if (from == null) return null
        var inString = false
        var pos: Int = from
        var level = 0
        for (c in input.substring(from)) {
            if (!inString) {
                if (c == '"') {
                    inString = true
                } else if (c == ch) {
                    if (!levelParens || level == 0) {
                        return pos
                    }
                } else if (c == '(') {
                    level++
                } else if (c == ')') {
                    level--
                }
            } else {
                if (c == '"') {
                    inString = input[pos - 1] == '\\'
                }
            }
            pos++
        }
        return null
    }


    private fun parseRequestMapping(input: String, start: Int): RequestMapping? {
        val openParenPos: Int? = posOfFirst(input, start, '(')
        val closeParenPos: Int? = posOfFirst(input, openParenPos, ')')
        if (openParenPos != null && closeParenPos != null) {
            val mappings = mappings(input, openParenPos)
            if (mappings != null) {
                return RequestMapping(input, mappings, openParenPos, closeParenPos)
            }
        }
        return null
    }

    private fun withMultipleRegex(vararg regex: Regex, block: Regex.() -> String?): String? {
        for (r in regex) {
            val res = r.block()
            if (res != null) {
                return res
            }
        }
        return null
    }

    private fun mappings(input: String, from: Int): String? {
        return withMultipleRegex(
                Regex("""\s*\(\s*"([^"]+)""""),
                Regex("""\s*\(\s*value\s*=\s*"([^"]+)"""")
        ) {
            find(input.substring(from))?.groupValues?.elementAtOrNull(1)
        }
    }


    data class RequestMapping(val input: String, val mappings: String, val start: Int, val end: Int) {
        override fun toString(): String {
            return "RequestMapping[start=$start, end=$end, mappings=${mappings}, input='${input.substring(start, end + 1)}']"
        }
    }

    private fun parseMethod(from: Int, input: String, mappings: String): Method? {
        val re = Regex("""public\s+([^)(]+)\s+([^\s(]+)\(""")
        val match = re.find(input.substring(from))
        val groups = match?.groupValues
        @Suppress("SpellCheckingInspection")
        if (groups?.size == 3 && !groups[1].startsWith("class")) {
            val fname = groups[2]

            val params = parseParams(input, from + match.range.last)
            return Method(fname, params, from, mappings)
        }
        return null
    }

    private fun parseParams(input: String, from: Int): List<String> {
        val to = matchingParen(input, from)
        if (to != null) {
            return splitBy(',', input.substring(from + 1, to))
                    .map { it.trim().removeSurrounding(",") }.map { param ->
                        val re = Regex(""".*\s+(String|int)\s+([^\s]+)\s*""", RegexOption.MULTILINE)
                        val m = re.matchEntire(param)
                        if (m?.groupValues != null) {
                            m.groupValues.let {
                                it[2] // name
                            }
                        } else {
                            null
                        }
                    }.filterNotNull()
        }
        return listOf()
    }

    @Suppress("SameParameterValue")
    private fun splitBy(c: Char, input: String): List<String> {
        var s = input
        val res = mutableListOf<String>()
        while (s.isNotEmpty()) {
            val p = posOfFirst(s, 0, c, true)
            if (p != null) {
                res.add(s.substring(0, p))
                s = s.substring(p + 1).trim()
            } else {
                res.add(s)
                return res
            }
        }
        return res
    }

    private fun matchingParen(input: String, from: Int): Int? {
        var inString = false
        var pos: Int = from
        var level = 0

        for (c in input.substring(from)) {
            if (!inString) {
                if (c == '"') {
                    inString = true
                } else {
                    if (c == '(') {
                        level++
                    }
                    if (c == ')') {
                        level--
                        if (level == 0) {
                            return pos
                        }
                    }
                }
            } else {
                if (c == '"') {
                    inString = input[pos - 1] == '\\'
                }
            }
            pos++
        }
        return null
    }

    data class Method(val name: String, val params: List<String>, val loc: Int, val mappings: String)



    private fun processFile0(file: File) {
        var text = file.readText()
//        foo(text)

        val requestMappingRe = Regex("""@RequestMapping\s*\(""")
        val matches = requestMappingRe.findAll(text)
        val rm = matches.map {
            parseRequestMapping(text, it.range.first)
        }.filterNotNull()
        val rams = rm.map {
            parseMethod(it.end + 1, text, it.mappings)
        }.filterNotNull()

        rams.toList().reversed().forEach { method ->
            println("processing method $method")
            val loc = posOfFirst(text, method.loc, '{')
            val ndc = formatNdc(method, file.nameWithoutExtension)
            if (loc != null) {
                text = text.replaceRange(loc + 1, loc + 1, "\n$ndc;")
            } else {
                println("*** Fail to find location to insert fix for method $method")
            }
        }
//        println(text)
        file.writeText(text)
//
    }

    // open  fun announceEnter(methodName: kotlin.String?, log: Logger?, vararg args: kotlin.Any?)
    private fun formatNdc(method: Method, loc: String): String {
        val params = method.params.flatMap {
            listOf("\"$it\"", it)
        }.joinToString(", ").let {
            if(it.isNotEmpty())
                ", $it"
            else
                it
        }

//        return """    logger.announceEnter("${method.name}"$params)"""
        return """    com.saaspulse.log.Slf4JExt.announceEnter(log, "${method.name}"$params)"""
    }

//    public class Slf4JExt {
//        public static void announceEnter(Logger logger, String methodName, Object... args) {

            fun process(dir: File, file: File) {
        file.forEachLine { line ->
            val f = File(dir, line.trim())
            println("processing file ${f.absolutePath}")
            processFile0(f)
        }
    }


}

@ExperimentalContracts
fun main() {
    NDSProcessor().process(File("/Users/barak/dev/totango/main"),
            File("/Users/barak/dev/totango/main/files.txt"))
}


// find . -name "*.java" -print0 | xargs -0 egrep -i 'NDC.push\(String.format\("%s:%s", \"[^\"]+\"'
// cat ./application/src/main/java/com/saaspulse/web/api/ActivitiesInventoryController.java | sed -ei 's/NDC.push[(]String.format[(]"%s:%s", "\([^"]*\)",/NDC.push(String.format("\1:%s",'/


//sed -ei 's/NDC.push[(]String.format[(]"%s:%s", "\([^"]*\)",/NDC.push(String.format("\1:%s",'/