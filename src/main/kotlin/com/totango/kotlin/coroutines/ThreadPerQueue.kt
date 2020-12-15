@file:Suppress("UNUSED_PARAMETER", "unused")

package com.totango.kotlin.coroutines

data class Item(val value: Int)

class Queue {
    fun take(): Item {
        return Item(1)
    }
}

class Db {
    fun readKey1(item: Item): String {
        return "key1"
    }

    fun readKey2(item: Item): String {
        return "key2"
    }

    fun update(item: Item, key1: String, key2: String): String {
        return "end"
    }
}

// note, we are not exploiting the fact that readKey1 and readKey2 does not have data dependency
class ThreadPerQueue {
    fun run(queues: List<Queue>, db: Db) {
        for (queue in queues) {
            Thread {
                while (!Thread.interrupted()) {
                    try {
                        val item = queue.take()
                        val key1 = db.readKey1(item)
                        val key2 = db.readKey2(item)
                        db.update(item, key1, key2)
                    } catch (int: InterruptedException) {
                        Thread.currentThread().interrupt()
                    } catch (err: Exception) {
                        // handle
                    }
                }
            }.start()
        }
    }
}
