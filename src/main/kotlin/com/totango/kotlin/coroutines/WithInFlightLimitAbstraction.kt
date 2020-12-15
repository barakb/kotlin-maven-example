package com.totango.kotlin.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


// inFlight coroutines that executing tasks from the channel.
fun CoroutineScope.processWithInFlightLimit(inFlight: Int, channel: Channel<suspend () -> Unit>) {
    repeat(inFlight) {
        launch {
            for (task in channel) {
                task()
            }
        }
    }
}

fun main() = runBlocking {
    val inFlight = 3
    val queues = List(10) { QueueC("$it") }

    // Backlog of tasks to be executed saved in a centraql channel
    val channel = Channel<suspend () -> Unit>(2)
    val db = DbC()
    // tThis code looks almost like the code without the in flight control, instead fo processing the item
    // we pack it into a command and send the command to the channel.
    // Spawn a coroutine for each queue
    for (queue in queues) {
        launch {
            while (true) {
                val item = queue.take()
                channel.send{
                    val key1 = db.readKey1(item)
                    println("Thread [${Thread.currentThread().name}] got key1 for $item")
                    val key2 = db.readKey2(item)
                    println("Thread [${Thread.currentThread().name}] got key2 for $item")
                    db.update(item, key1, key2)
                    println("Thread [${Thread.currentThread().name}] call update for $item")
                }
            }
        }
    }
    processWithInFlightLimit(inFlight, channel)
}


