package tutorial

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.storm.Config
import org.apache.storm.StormSubmitter
import org.apache.storm.spout.SpoutOutputCollector
import org.apache.storm.task.OutputCollector
import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichSpout
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.trident.TridentTopology
import org.apache.storm.trident.operation.*
import org.apache.storm.trident.tuple.TridentTuple
import org.apache.storm.trident.windowing.InMemoryWindowsStoreFactory
import org.apache.storm.tuple.Fields
import org.apache.storm.tuple.Tuple
import org.apache.storm.tuple.Values
import org.apache.storm.utils.Utils
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


object TridentExample {
    @kotlin.Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String?>?) {
        val topology = TridentTopology()
        val filteredStream = topology.newStream("spout", RandomNumberSpout())
            .filter(SimpleFilter())
        val aggStream1 = filteredStream.tumblingWindow(
            BaseWindowedBolt.Duration(10, TimeUnit.SECONDS), InMemoryWindowsStoreFactory(), Fields("operation"), SumAsAggregator(), Fields("sumOfOperations1")
        ).each(RandomKey(), Fields("key"))
        val aggStream2 = filteredStream.tumblingWindow(
            BaseWindowedBolt.Duration(5, TimeUnit.SECONDS), InMemoryWindowsStoreFactory(), Fields("operation"), SumAsAggregator(), Fields("sumOfOperations2")
        ).each(RandomKey(), Fields("key"))
        topology.join(listOf(aggStream1, aggStream2), listOf(Fields("key"), Fields("key")), Fields("key", "sumOfOperations1", "sumOfOperations2"))
            .each(Fields("key", "sumOfOperations1", "sumOfOperations2"),WriteToFile("/data/outputTrident.txt"), Fields())
        // Create a config object.
        val conf = Config()

        // Turn on debugging mode.
        conf.setDebug(true)

        conf.setNumWorkers(1)
        StormSubmitter.submitTopology("trident-example", conf, topology.build())
    }

    class WriteToFile(private val filePath: String): BaseFunction() {
        private var writer: BufferedWriter? = null
        private var objectMapper: ObjectMapper? = null
        override fun cleanup() {
            try {
                writer!!.close()
            } catch (e: IOException) {
                DemoTopology.FileWritingBolt.logger.error("Failed to close writer!")
            }
        }
        override fun prepare(topoConf: MutableMap<String, Any>?, context: TridentOperationContext?) {
            objectMapper = ObjectMapper()
            objectMapper!!.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            try {
                writer = BufferedWriter(FileWriter(filePath))
            } catch (e: IOException) {
                DemoTopology.FileWritingBolt.logger.error("Failed to open a file for writing.", e)
            }
        }

        override fun execute(tuple: TridentTuple, collector: TridentCollector?) {
            val key = tuple.getStringByField("key")
            val sumOfOperations1 = tuple.getIntegerByField("sumOfOperations1")
            val sumOfOperations2 = tuple.getIntegerByField("sumOfOperations2")
            val aggregatedWindow = listOf(
                key, sumOfOperations1, sumOfOperations2
            )
            try {
                writer!!.write(objectMapper!!.writeValueAsString(aggregatedWindow))
                writer!!.newLine()
                writer!!.flush()
            } catch (e: IOException) {
                DemoTopology.FileWritingBolt.logger.error("Failed to write data to file.", e)
            }

        }
    }

    class RandomKey: BaseFunction() {
        override fun execute(tuple: TridentTuple?, collector: TridentCollector?) {
            val list = listOf("key1", "key2", "key3", "key4", "key5")
            val randomIndex = Random().nextInt(list.size);
            collector!!.emit(Values(list[randomIndex]))
        }
    }

    class SimpleFilter : BaseFilter() {
        override fun isKeep(tuple: TridentTuple): Boolean {
            return tuple.getIntegerByField("operation") > 10
        }
    }

    class SumAsAggregator : BaseAggregator<SumAsAggregator.Sum>() {
        override fun init(batchId: Any, collector: TridentCollector): Sum {
            return Sum()
        }

        override fun aggregate(sum: Sum, tuple: TridentTuple, collector: TridentCollector) {
            sum.sumOfOperations += tuple.getIntegerByField("operation")
        }

        override fun complete(sum: Sum, collector: TridentCollector) {
            collector.emit(Values(sum.sumOfOperations))
            sum.sumOfOperations = 0
        }

        class Sum {
            var sumOfOperations: Int = 0
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
            outputCollector!!.emit(Values(random?.nextInt(100), System.currentTimeMillis()))
        }

        override fun declareOutputFields(outputFieldsDeclarer: OutputFieldsDeclarer) {
            outputFieldsDeclarer.declare(Fields("operation", "timestamp"))
        }
    }

    }