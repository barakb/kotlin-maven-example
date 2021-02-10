@file:JvmName("StringFunctions")

package join


fun <T> joinToString1(
    collection: Collection<T>,
    separator: String,
    prefix: String,
    postfix: String
): String {

    val result = StringBuilder(prefix)

    for ((index, element) in collection.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(element)
    }

    result.append(postfix)
    return result.toString()
}

// default params

@JvmOverloads
fun <T> joinToString2(
    collection: Collection<T>,
    separator: String = ",",
    prefix: String = "",
    postfix: String = ""
): String {

    val result = StringBuilder(prefix)

    for ((index, element) in collection.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(element)
    }

    result.append(postfix)
    return result.toString()
}

//Getting rid of static utility classes: top-level functions and properties



fun <T> Collection<T>.joinToString3(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = ""
): String {
    val result = StringBuilder(prefix)

    for ((index, element) in this.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(element)
    }

    result.append(postfix)
    return result.toString()
}




fun main(args: Array<String>) {
    val list = listOf(1, 2, 3)
    println(joinToString1(list, "; ", "(", ")"))
    println(joinToString2(list, "; "))
    println(list.joinToString3(postfix = "]"))
    list.joinToString(transform = {(it + 1).toString()})
}
