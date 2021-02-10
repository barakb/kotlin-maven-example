
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.asFlow
import reactor.kafka.receiver.ReceiverOptions

import org.apache.kafka.common.serialization.StringDeserializer

import org.apache.kafka.clients.consumer.ConsumerConfig

import org.apache.kafka.common.serialization.IntegerDeserializer

import java.util.HashMap
import reactor.kafka.receiver.KafkaReceiver

import reactor.kafka.receiver.ReceiverRecord

import reactor.core.publisher.Flux







fun main(){
    val bootstrapServers = ""
    val topic = ""
    val consumerProps: MutableMap<String, Any> = HashMap()
    consumerProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
    consumerProps[ConsumerConfig.GROUP_ID_CONFIG] = "sample-group"
    consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = IntegerDeserializer::class.java
    consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java


    val receiverOptions: ReceiverOptions<Int, String> = ReceiverOptions.create<Int, String>(consumerProps)
        .subscription(listOf(topic))
    val inboundFlux: Flux<ReceiverRecord<Int, String>> = KafkaReceiver.create(receiverOptions)
        .receive()


    val ch = Channel<List<ReceiverRecord<Int, String>>>(1)
    val flow = inboundFlux.buffer(1).asFlow()
    flow.onEach { ch.send(it)  }


}