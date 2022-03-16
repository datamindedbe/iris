package tutorial

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.storm.Config
import org.apache.storm.StormSubmitter
import org.apache.storm.bolt.JoinBolt
import org.apache.storm.kafka.bolt.KafkaBolt
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector
import org.apache.storm.kafka.spout.KafkaSpout
import org.apache.storm.kafka.spout.KafkaSpoutConfig
import org.apache.storm.spout.SpoutOutputCollector
import org.apache.storm.task.OutputCollector
import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.BasicOutputCollector
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.topology.base.BaseBasicBolt
import org.apache.storm.topology.base.BaseRichBolt
import org.apache.storm.topology.base.BaseRichSpout
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.tuple.Fields
import org.apache.storm.tuple.Tuple
import org.apache.storm.tuple.Values
import org.apache.storm.utils.Utils
import org.apache.storm.windowing.TupleWindow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


fun writeFilteredFile(tuple: Tuple): List<Any> {
    val operations = tuple.getIntegerByField("operation")
    val timestamp = tuple.getLongByField("timestamp")
    return listOf(
        operations, timestamp
    )
}

object NumberTopology {
    @kotlin.Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String?>?) {
        val builder = TopologyBuilder()

        builder.setSpout(
            "kafka_spout", KafkaSpout(
                KafkaSpoutConfig.builder("kafka:9092", "input-topic")
                    .setProp(ConsumerConfig.GROUP_ID_CONFIG, "storm-kafka-group")
                    .build()
            ), 1
        )

        val kafkanumberBolt: KafkaNumberBolt = KafkaNumberBolt()
        builder.setBolt("kafka_number_bolt", kafkanumberBolt)
            .shuffleGrouping("kafka_spout")

        val filtering: FilteringBolt = FilteringBolt()
        builder.setBolt("filteringBolt", filtering)
            .shuffleGrouping("kafka_number_bolt")

        val aggregating1: BaseWindowedBolt? = AggregatingBolt()
//            .withTimestampField("timestamp")
            .withTumblingWindow(BaseWindowedBolt.Duration(5, TimeUnit.SECONDS))
//            .withLag(BaseWindowedBolt.Duration.seconds(1))
//            .withWindow(BaseWindowedBolt.Duration.seconds(5))

        builder.setBolt("aggregatingBolt1", aggregating1)
            .shuffleGrouping("filteringBolt")

        val aggregating2: BaseWindowedBolt? = AggregatingBolt()
            .withTumblingWindow(BaseWindowedBolt.Duration(10, TimeUnit.SECONDS))
        builder.setBolt("aggregatingBolt2", aggregating2)
            .shuffleGrouping("filteringBolt")

        val joinBolt: JoinBolt = JoinBolt("aggregatingBolt1", "key")
            .join("aggregatingBolt2", "key", "aggregatingBolt1")
            .select("aggregatingBolt1:key, aggregatingBolt1:sumOfOperations, aggregatingBolt2:sumOfOperations")
            .withTumblingWindow(
                BaseWindowedBolt.Duration(10, TimeUnit.SECONDS)
            )

        builder.setBolt("joinBolt", joinBolt, 1)
            .fieldsGrouping("aggregatingBolt1", Fields("key"))
            .fieldsGrouping("aggregatingBolt2", Fields("key"))

        val kafkaProps = Properties()
        kafkaProps["bootstrap.servers"] = "kafka:9092"
        kafkaProps["acks"] = "1"
        kafkaProps["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        kafkaProps["value.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"

        val kafkaOutputBolt = KafkaBolt<Any?, Any?>()
            .withProducerProperties(kafkaProps)
            .withTopicSelector(DefaultTopicSelector("output-topic"))
            .withTupleToKafkaMapper(FieldNameBasedTupleToKafkaMapper())
        builder.setBolt("forwardToKafka", kafkaOutputBolt, 2).shuffleGrouping("joinBolt")

        builder.setSpout(
            "kafka_spout_2", KafkaSpout(
                KafkaSpoutConfig.builder("kafka:9092", "output-topic")
                    .setProp(ConsumerConfig.GROUP_ID_CONFIG, "storm-kafka-group")
                    .build()
            ), 1
        )

        val filePath = "/data/output.txt"
        val file: FileWritingBolt = FileWritingBolt(filePath)
        builder.setBolt("fileBolt", file)
            .shuffleGrouping("joinBolt")

        val filePath2 = "/data/output2.txt"
        val file2: FileWritingBolt = FileWritingBolt(filePath2)
        builder.setBolt("fileBolt2", file2)
            .shuffleGrouping("kafka_spout_2")

        // Create a config object.
        val conf = Config()

        // Turn on debugging mode.
        conf.setDebug(true)

        conf.setNumWorkers(3)
        StormSubmitter.submitTopology("number-topology", conf, builder.createTopology())
    }

    class KafkaNumberBolt : BaseBasicBolt() {
        data class InputMessage(
            var number: Int
        )

        override fun execute(tuple: Tuple, basicOutputCollector: BasicOutputCollector) {
            print(tuple)

            val operation = jacksonObjectMapper().readValue<InputMessage>(tuple.getStringByField("value"))
            val output = listOf(
                operation.number, System.currentTimeMillis()
            )
            basicOutputCollector.emit(output)


        }

        override fun declareOutputFields(outputFieldsDeclarer: OutputFieldsDeclarer) {
            outputFieldsDeclarer.declare(Fields("operation", "timestamp"))
        }
    }

    class RandomNumberSpout : BaseRichSpout() {
        private var random: Random? = null
        private var outputCollector: SpoutOutputCollector? = null
        override fun open(
            conf: MutableMap<String, Any>?,
            topologyContext: TopologyContext?,
            spoutOutputCollector: SpoutOutputCollector?
        ) {
            random = Random()
            outputCollector = spoutOutputCollector
        }

        override fun nextTuple() {
            Utils.sleep(10)
            outputCollector!!.emit(Values(random?.nextInt(), System.currentTimeMillis()))
        }

        override fun declareOutputFields(outputFieldsDeclarer: OutputFieldsDeclarer) {
            outputFieldsDeclarer.declare(Fields("operation", "timestamp"))
        }
    }

    class FilteringBolt : BaseBasicBolt() {
        override fun execute(tuple: Tuple, basicOutputCollector: BasicOutputCollector) {
            val operation = tuple.getIntegerByField("operation")
            if (operation > 10) {
                basicOutputCollector.emit(tuple.values)
            }
        }

        override fun declareOutputFields(outputFieldsDeclarer: OutputFieldsDeclarer) {
            outputFieldsDeclarer.declare(Fields("operation", "timestamp"))
        }
    }

    class AggregatingBolt : BaseWindowedBolt() {
        private var outputCollector: OutputCollector? = null
        override fun prepare(
            topoConf: MutableMap<String, Any>?,
            context: TopologyContext?,
            collector: OutputCollector?
        ) {
            outputCollector = collector
        }

        override fun declareOutputFields(declarer: OutputFieldsDeclarer) {
            declarer.declare(Fields("sumOfOperations", "key"))
        }

        override fun execute(tupleWindow: TupleWindow) {
            val tuples = tupleWindow.get()
//            tuples.sortWith(Comparator.comparing { tuple: Tuple -> getTimestamp(tuple) })
            val sumOfOperations = tuples.stream()
                .mapToInt { tuple: Tuple -> tuple.getIntegerByField("operation") }
                .sum()
//            val beginningTimestamp = getTimestamp(tuples[0])
//            val endTimestamp = getTimestamp(tuples[tuples.size - 1])
            val list = listOf("key1", "key2", "key3", "key4", "key5")
            val randomIndex = Random().nextInt(list.size);
            val randomKey = list[randomIndex]
            val values = Values(sumOfOperations, randomKey)
            outputCollector!!.emit(values)
        }

        private fun getTimestamp(tuple: Tuple): Long {
            return tuple.getLongByField("timestamp")
        }
    }

    //    class FileWritingBolt(private val filePath: String, private val writerFun: Function<Tuple>) : BaseRichBolt() {
    class FileWritingBolt(private val filePath: String) : BaseRichBolt() {
        private var writer: BufferedWriter? = null
        private var objectMapper: ObjectMapper? = null
        override fun cleanup() {
            try {
                writer!!.close()
            } catch (e: IOException) {
                logger.error("Failed to close writer!")
            }
        }

        override fun declareOutputFields(declarer: OutputFieldsDeclarer) {
            declarer.declare(Fields())
        }

        override fun prepare(
            topoConf: MutableMap<String, Any>?,
            topologyContext: TopologyContext?,
            outputCollector: OutputCollector?
        ) {
            objectMapper = ObjectMapper()
            objectMapper!!.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            try {
                writer = BufferedWriter(FileWriter(filePath))
            } catch (e: IOException) {
                logger.error("Failed to open a file for writing.", e)
            }
        }

        override fun execute(tuple: Tuple) {
            val key = tuple.getStringByField("aggregatingBolt1:key")
            val sumOfOperations1 = tuple.getIntegerByField("aggregatingBolt1:sumOfOperations")
            val sumOfOperations2 = tuple.getIntegerByField("aggregatingBolt2:sumOfOperations")
            val aggregatedWindow = listOf(
                key, sumOfOperations1, sumOfOperations2
            )
            try {
                writer!!.write(objectMapper!!.writeValueAsString(aggregatedWindow))
                writer!!.newLine()
                writer!!.flush()
            } catch (e: IOException) {
                logger.error("Failed to write data to file.", e)
            }

        } // public constructor and other methods

        companion object {
            var logger: Logger = LoggerFactory.getLogger(FileWritingBolt::class.java)
        }
    }
}



