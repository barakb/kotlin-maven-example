package com.totango.kotlin.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

typealias Task = suspend () -> Unit

// inFlight coroutines that executing tasks from the channel.
fun CoroutineScope.processWithInFlightLimit(inFlight: Int, channel: ReceiveChannel<Task>) {
    repeat(inFlight) {
        launch {
            for (task in channel) {
                task()
            }
        }
    }
}

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val inFlight = 3
    val queues = List(10) { QueueC("$it") }

    // Backlog of tasks to be executed saved in a centraql channel
//    val channel = Channel<suspend () -> Unit>(2)
    val db = DbC()
    // tThis code looks almost like the code without the in flight control, instead fo processing the item
    // we pack it into a command and send the command to the channel.
    // Spawn a coroutine for each queue
    val channel = produce<Task>(capacity = 2) {
        for (queue in queues) {
            launch {
                while (true) {
                    val item = queue.take()
                    channel.send {
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
    }
    processWithInFlightLimit(inFlight, channel)
}


