package com.github.barakb

import java.io.File

fun main() {
    val dir = File("/Users/barak/dev/totango/main")
    File(dir,"files.txt").forEachLine {file ->
        println("procesing file ${File(dir, file).absolutePath}")
        process(File(dir, file))
    }
}

fun process(file: File) {
    var text = file.readText()
    val r = Regex("ProcessorLogger.(info|debug|warn|error)\\s*\\(")
    val matches = r.findAll(text)
    val pairs = mutableListOf<Pair<Param, String>>()
    for (match in matches) {
        val start = match.range.last
        val endPos: Int = findEndPos(start, text)
        val params = splitParams(text, start + 1, endPos)
        if (params.size == 2) {
            val param2Components = splitComponents(params[1].text)
            if(1 < param2Components.size){
//                println("match : [$start - $endPos] ${text.substring(start, endPos)}")
//                println("params2: ${params[1]}")
//                println("split components: $param2Components")
                val transformed = transform(param2Components)
//                println("transformed: $transformed\n")
                pairs.add(params[1] to transformed)
            }
        }
    }
    if(pairs.isNotEmpty()) {
//        println("pairs: $pairs")
        for ((param, fix) in pairs.reversed()) {
            text = applyFix(fix, param.start, param.end, text)
        }
        //println("fix:\n$text")
        println("${pairs.size} fixes at file: ${file.absolutePath}")
        file.writeText(text)
    }

}

fun applyFix(fix: String, start: Int, end: Int, text: String): String {
    return text.replaceRange(start, end, fix)
}

fun transform(args: List<String>): String {
    var acc = ""
    val params = mutableListOf<String>()
    for(arg in args){
        if(arg[0] == '"'){
            acc += arg.subSequence(1, arg.length - 1)
        }else{
            acc += "{}"
            params.add(arg)
        }
    }
    acc = '"' + acc + '"'
    if(params.isNotEmpty()){
        acc = acc + ", " +params.joinToString (separator = ", ")
    }
    return acc
}

fun splitComponents(text: String): List<String> {
    // split by + and for each elm trim ws
    var inString = false
    var pos = 0
    var start = 0
    var parens = 0
    val res = mutableListOf<String>()
    for (c in text) {
        if (!inString) {
            if(c == '+' && parens == 0){
                res.add(text.substring(start, pos).trim())
                start = pos + 1
            }
            if(c == '"'){
                inString = true
            }
            if(c == '('){
                parens += 1
            }
            if(c == ')'){
                parens -= 1
            }
        }else{
            if (c == '"') {
                inString = text[pos - 1] == '\\'
            }
        }
        pos += 1
    }
    val last = text.substring(start, pos).trim()
    if(last.isNotEmpty()){
        res.add(last)
    }
    return res
}

fun splitParams(text: String, start: Int, end: Int): List<Param> {
    val res = mutableListOf<Param>()
    var pos = start
    while (pos < end) {
        val p = parseParam(text, pos)
        if (p != null) {
            res.add(p)
            pos = skipWhitSpaces(text, p.end + 1)
        } else {
            return res
        }
    }
    return res

}

fun skipWhitSpaces(text: String, pos: Int): Int {
    var ret = pos
    for (c in text.substring(pos)) {
        if (c == '\n' || c == '\t' || c == ' ') {
            ret += 1
        } else {
            return ret
        }
    }
    return ret
}

fun parseParam(text: String, start: Int): Param? {
    var pos = start
    var inString = false
    var parens = 0
    for (c in text.substring(start)) {
        if (!inString) {
            if (c == ',' && parens == 0) {
                return Param(start, pos, text.substring(start, pos))
            }
            if (c == '(') {
                parens += 1
            }
            if (c == ')') {
                if (parens == 0) {
                    return Param(start, pos, text.substring(start, pos))
                } else {
                    parens -= 1
                }
            }
            if (c == '"') {
                inString = true
            }
        } else {
            if (c == '"') {
                inString = text[pos - 1] == '\\'
            }
        }
        pos += 1
    }
    return null
}

data class Param(val start: Int, val end: Int, val text: String)

fun findEndPos(start: Int, text: String): Int {
    var pos = start
    var inString = false
    while (true) {
        val ch = text[pos]
        if (!inString) {
            if (ch == ';') {
                return pos
            }
            if (ch == '"') {
                inString = true
            }
        } else {
            if (ch == '"') {
                inString = text[pos - 1] == '\\'
            }
        }
        pos += 1
    }

}
