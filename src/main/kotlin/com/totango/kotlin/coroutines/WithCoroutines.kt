@file:Suppress("UNUSED_PARAMETER", "unused")

package com.totango.kotlin.coroutines

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

suspend fun randomDelay() {
    delay((10 .. 100).random().toLong())
}

class QueueC(val name: String) {
    private val value = AtomicInteger(0)
    suspend fun take(): Item {
        randomDelay()
        return Item(value.incrementAndGet())
    }
}

class DbC {
    suspend fun readKey1(item: Item): String {
        randomDelay()
        return "key1"
    }

    suspend fun readKey2(item: Item): String {
        randomDelay()
        return "key2"
    }

    suspend fun update(item: Item, key1: String, key2: String): String {
        randomDelay()
        return "end"
    }
}


class WithCoroutines {
    suspend fun run(queue: QueueC, db: DbC) {
        while (true) {
            try {
                val item = queue.take()
                println("Thread [${Thread.currentThread().name}] took $item from queue ${queue.name}")
                val key1 = db.readKey1(item)
                println("Thread [${Thread.currentThread().name}] got key1 for $item")
                val key2 = db.readKey2(item)
                println("Thread [${Thread.currentThread().name}] got key2 for $item")
                db.update(item, key1, key2)
                println("Thread [${Thread.currentThread().name}] call update for $item")
            } catch (can: CancellationException) {
                return
            } catch (err: Exception) {
                // handle
            }
        }
    }

    suspend fun runWithDataDependencies(queue: QueueC, db: DbC, scope: CoroutineScope) {
        while (true) {
            try {
                val item = queue.take()
                println("Thread [${Thread.currentThread().name}] took $item from queue ${queue.name}")
                val key1 = scope.async { db.readKey1(item) }
                println("Thread [${Thread.currentThread().name}] got key1 for $item")
                val key2 = scope.async{ db.readKey2(item) }
                println("Thread [${Thread.currentThread().name}] got key2 for $item")
                db.update(item, key1.await(), key2.await())
                println("Thread [${Thread.currentThread().name}] call update for $item")
            } catch (can: CancellationException) {
                return
            } catch (err: Exception) {
                // handle
            }
        }
    }

}

fun main() = runBlocking {
    val queues = List(10) { QueueC("$it") }
    val db = DbC()
    val processor = WithCoroutines()
    for (queue in queues) {
        launch {
//            processor.run(queue, db)
            processor.runWithDataDependencies(queue, db, this)
        }
    }
}