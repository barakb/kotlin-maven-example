@file:Suppress("unused", "UNUSED_PARAMETER")

package com.totango.kotlin.coroutines

import java.util.concurrent.CompletableFuture


class QueueF {
    fun take(): CompletableFuture<Item> {
        return CompletableFuture.completedFuture(Item(7))
    }
}

class DbF {
    fun readKey1(item: Item): CompletableFuture<String> {
        return CompletableFuture.completedFuture("key1")
    }

    fun readKey2(item: Item): CompletableFuture<String> {
        return CompletableFuture.completedFuture("key2")
    }

    fun update(item: Item, key1: String, key2: String): CompletableFuture<String> {
        return CompletableFuture.completedFuture("end")
    }
}

class UsingFutures {
    fun run(queues: List<QueueF>, db: DbF) {
        fun runOne(queue: QueueF) {
            while(true) {
                queue.take()
                    .thenCompose { item ->
                        db.readKey1(item).thenCombine(db.readKey2(item)) { k1, k2 ->
                            Triple(item, k1, k2)
                        }
                    }.thenCompose { (item, key1, key2) ->
                        db.update(item, key1, key2)
                    }.handle { _, _ ->
                        // at least I can handle errors in one place at the end.
                        runOne(queue)
                    }
            }
        }
        for (queue in queues) {
            runOne(queue)
        }
    }
}