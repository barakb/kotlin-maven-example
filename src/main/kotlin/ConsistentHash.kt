import java.math.BigInteger
import java.security.MessageDigest
import java.util.*


class ConsistentHash<T>(
    private var numberOfReplicas: Int = 1,
    private var hash: (String) -> Int = ::hash,
) {

    private val circle: SortedMap<Int, T> = TreeMap()

    fun add(vararg nodes: T) {
        for (node in nodes) {
            for (i in 0 until numberOfReplicas) {
                circle[hash("$node:$i")] = node
            }
        }
    }

    fun remove(vararg nodes: T) {
        for (node in nodes) {
            for (i in 0 until numberOfReplicas) {
                circle.remove(hash("$node:$i"))
            }
        }
    }

    operator fun get(key: Any?): T? {
        if (circle.isEmpty()) {
            return null
        }
        var hash = hash("$key")
        if (!circle.containsKey(hash)) {
            val tailMap = circle.tailMap(hash)
            hash = if (tailMap.isEmpty()) circle.firstKey() else tailMap.firstKey()
        }
        return circle[hash]
    }

}

fun hash(key: String): Int = (BigInteger(keyToMd5Hex(key), 16) % Int.MAX_VALUE.toBigInteger()).toInt()

fun keyToMd5Hex(key: String): String {
    val digest = MessageDigest.getInstance("MD5").digest(key.toByteArray())
    val sb = StringBuilder()
    for (b in digest) {
        sb.append(String.format("%02x", b))
    }
    return sb.toString()
}

fun main() {
    val consistentHash = ConsistentHash<String>(100)
//    consistentHash.add("Newton", "Einstein", "Turing")
    consistentHash.add("Newton", "Turing")
    val pairs = List(20) {
        val node = consistentHash[it]
        (node to it)
    }
    val res = pairs.groupBy {
        it.first
    }.mapValues {
        it.value.map { pair->
            pair.second
        }
    }
    println("res: $res")
   // res: {Newton=[0, 3, 4, 9, 11, 13, 15, 16, 18], Einstein=[1, 5, 7, 8, 10, 14, 17], Meir=[2, 6], Turing=[12, 19]}
   // res: {Newton=[0, 3, 4, 6, 9, 11, 13, 15, 16, 18], Einstein=[1, 5, 7, 8, 10, 14, 17], Turing=[2, 12, 19]}
   // res: {Newton=[0, 1, 3, 4, 5, 6, 7, 8, 9, 11, 13, 15, 16, 18], Turing=[2, 10, 12, 14, 17, 19]}
}