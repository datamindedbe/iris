package tutorial

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.storm.Config
import org.apache.storm.StormSubmitter
import org.apache.storm.bolt.JoinBolt
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


object DemoTopology {
    @kotlin.Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String?>?) {
        val builder = TopologyBuilder()

        val random = RandomNumberSpout()
        builder.setSpout("randomNumberSpout", random)


        val filtering: FilteringBolt = FilteringBolt()
        builder.setBolt("filteringBolt", filtering)
            .shuffleGrouping("randomNumberSpout")

        val aggregating1: BaseWindowedBolt? = AggregatingBolt()
            .withTimestampField("timestamp")
            .withTumblingWindow(BaseWindowedBolt.Duration(1000, TimeUnit.MILLISECONDS))

        builder.setBolt("aggregatingBolt1", aggregating1)
            .shuffleGrouping("filteringBolt")

        val aggregating2: BaseWindowedBolt? = AggregatingBolt()
            .withTumblingWindow(BaseWindowedBolt.Duration(500, TimeUnit.MILLISECONDS))
        builder.setBolt("aggregatingBolt2", aggregating2)
            .shuffleGrouping("filteringBolt")

        val joinBolt: JoinBolt = JoinBolt("aggregatingBolt1", "key")
            .join("aggregatingBolt2", "key", "aggregatingBolt1")
            .select("aggregatingBolt1:key, aggregatingBolt1:sumOfOperations, aggregatingBolt2:sumOfOperations")
            .withTumblingWindow(
                BaseWindowedBolt.Duration(5, TimeUnit.SECONDS)
            )

        builder.setBolt("joinBolt", joinBolt, 1)
            .fieldsGrouping("aggregatingBolt1", Fields("key"))
            .fieldsGrouping("aggregatingBolt2", Fields("key"))


        val filePath = "/data/output.txt"
        val file = FileWritingBolt(filePath)
        builder.setBolt("fileBolt", file)
            .shuffleGrouping("joinBolt")

        // Create a config object.
        val conf = Config()

        // Turn on debugging mode.
        conf.setDebug(true)

        conf.setNumWorkers(3)
        StormSubmitter.submitTopology("demo-topology", conf, builder.createTopology())
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
            val list = listOf("key1", "key2", "key3")
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



