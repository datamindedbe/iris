package tutorial

import org.apache.storm.spout.SpoutOutputCollector
import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichSpout
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.trident.TridentTopology
import org.apache.storm.trident.operation.Aggregator
import org.apache.storm.trident.operation.BaseFilter
import org.apache.storm.trident.operation.builtin.Sum
import org.apache.storm.trident.tuple.TridentTuple
import org.apache.storm.tuple.Fields
import org.apache.storm.tuple.Values
import org.apache.storm.utils.Utils
import java.util.*
import java.util.concurrent.TimeUnit


object TridentExample {
    @kotlin.Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String?>?) {
        val topology = TridentTopology()
        val filteredStream = topology.newStream("spout", RandomNumberSpout()).filter(SimpleFilter())
//        val aggStream1 = filteredStream.window(
//            BaseWindowedBolt.Duration(5, TimeUnit.SECONDS), Fields("operation"), Sum(), Fields("sumOfOperations")
//        )


    }

    class SimpleFilter : BaseFilter() {
        override fun isKeep(tuple: TridentTuple): Boolean {
            return tuple.getIntegerByField("operation") > 10
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