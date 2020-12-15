@file:Suppress("unused", "UNUSED_PARAMETER")

package com.totango.kotlin.coroutines


interface Callback<T> {
    fun success(value: T)
    fun fail(err: Exception)
}


class QueueCB {
    fun take(callback: Callback<Item>) {
        callback.success(Item(7))
    }
}

class DbCB {
    fun readKey1(item: Item, callback: Callback<String>) {
        callback.success("key1")
    }

    fun readKey2(item: Item, callback: Callback<String>) {
        callback.success("key2")
    }

    fun update(item: Item, key1: String, key2: String, callback: Callback<String>) {
        return callback.success("end")
    }
}

class OneThreadToRuleThemAll {
    fun run(queues: List<QueueCB>, db: DbCB) {
        fun process(queue: QueueCB) {
            queue.take(object : Callback<Item> {
                @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
                override fun success(item: Item) {
                    db.readKey1(item, object : Callback<String> {
                        override fun success(key1: String) {
                            db.readKey2(item, object : Callback<String> {
                                override fun success(key2: String) {
                                    db.update(item, key1, key2, object : Callback<String> {
                                        override fun success(value: String) {
                                            process(queue)
                                        }

                                        override fun fail(err: Exception) {
                                            TODO("Not yet implemented")
                                        }
                                    })
                                }

                                override fun fail(err: Exception) {
                                    TODO("Not yet implemented")
                                }
                            })
                        }

                        override fun fail(err: Exception) {
                            TODO("Not yet implemented")
                        }

                    })
                }

                override fun fail(err: Exception) {
                    TODO("Not yet implemented")
                }

            })
        }
        for (queue in queues) {
            process(queue)
        }
        // in fact the thread is free now
    }
}
