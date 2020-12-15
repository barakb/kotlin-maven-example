package com.totango.kotlin.coroutines

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking



fun main() = runBlocking {
    val inFlight = 3
    val queues = List(10) { QueueC("$it") }

    // use kotlin channel to collect data from all queues.
    // channel operations are suspendable.
    val channel = Channel<Item>(2)

    // Spawn a coroutine for each queue that transferring all the queue messages
    // to the central channel (in a loop).
    for (queue in queues) {
        launch {
            read(queue, channel)
        }
    }

    val db = DbC()
    // Spawn inFlight coroutines, each one read a message from the channel and
    // process the message, that way we can be sure that there are at most
    // inFlight messages in process on the same time.
    repeat(inFlight) {
        launch {
            for (item in channel) {
                processItem(db, item)
            }
        }
    }
}

suspend fun read(queue: QueueC, channel: Channel<Item>) {
    while (true) {
        channel.send(queue.take())
    }
}

private suspend fun processItem(db: DbC, item: Item) {
    val key1 = db.readKey1(item)
    println("Thread [${Thread.currentThread().name}] got key1 for $item")
    val key2 = db.readKey2(item)
    println("Thread [${Thread.currentThread().name}] got key2 for $item")
    db.update(item, key1, key2)
}


