package com.github.barakb

import java.io.File
import java.net.URLDecoder

fun main() {
    val dir = File("/Users/barak/Downloads")
    dir.listFiles()?.filter{ it.name.toLowerCase().endsWith(".txt")}?.forEach{ file ->
        println("processing file $file")
        processLogFile(file)
    }
}

fun splitParams(params: String) : Map<String, String?>{
    val seq = params.splitToSequence('&')
    return seq.map{
        val lst = it.splitToSequence('=').take(2).toList()
        if(lst.size == 2) {
            lst[0] to lst[1]
        }else{
            lst[0] to null
        }
    }.toMap()
}
fun processLogFile(file: File) {
    val text = file.readText()
    // "2020-11-24T23:13:24.105582Z gateway-collector-prod-lb 54.186.22.88:33660 10.0.112.50:8080 0.000027 0.000869 0.000021 200 200 0 35 \"GET https://sdr.totango.com:443/pixel.gif/"
    val r = Regex("""\n(\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d\.\d+Z)\s+gateway-collector-prod[^"]+"GET\s+https://sdr.totango.com:443/pixel.gif/\?([^\n]+)\s+HTTP/1.1""")
    val matches = r.findAll(text)
    println("matches ${matches.toList().size}")
//    matches.take(300).forEach {
    matches.forEach {
        println("date: ${it.groupValues[1]}")
        println("params: ${it.groupValues[2]}")
        val params = splitParams(it.groupValues[2])
        params.forEach{
//            println("decoding $it")
            val key=URLDecoder.decode(it.key,"utf-8")
            val value = it.value?.let {URLDecoder.decode(it,"utf-8")}
            println("key=$key value=$value")
        }
    }

//    val pairs = mutableListOf<Pair<Param, String>>()
//    for (match in matches) {
//        val start = match.range.last
//        val endPos: Int = findEndPos(start, text)
//        val params = splitParams(text, start + 1, endPos)
//        if (params.size == 2) {
//            val param2Components = splitComponents(params[1].text)
//            if (1 < param2Components.size) {
////                println("match : [$start - $endPos] ${text.substring(start, endPos)}")
////                println("params2: ${params[1]}")
////                println("split components: $param2Components")
//                val transformed = transform(param2Components)
////                println("transformed: $transformed\n")
//                pairs.add(params[1] to transformed)
//            }
//        }
}
