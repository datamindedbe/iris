package tutorial

import org.apache.storm.Config
import org.apache.storm.StormSubmitter
import org.apache.storm.task.OutputCollector
import org.apache.storm.task.TopologyContext
import org.apache.storm.testing.TestWordSpout
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.topology.base.BaseRichBolt
import org.apache.storm.tuple.Fields
import org.apache.storm.tuple.Tuple
import org.apache.storm.tuple.Values

object ExclamationTopology {
    @kotlin.Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String?>?) {
        val builder = TopologyBuilder()
        builder.setSpout("word", TestWordSpout(), 10)
        builder.setBolt("exclaim1", ExclamationBolt(), 3).shuffleGrouping("word")
        builder.setBolt("exclaim2", ExclamationBolt(), 2).shuffleGrouping("exclaim1")

        // Create a config object.
        val conf = Config()

        // Turn on debugging mode.
        conf.setDebug(true)

        // Set the number of workers for running all spout and bolt tasks.
        // If we have two supervisors with 4 allocated workers each, and this topology is
        // submitted to the master (Nimbus) node, then these 8 workers will be distributed
        // among the two supervisors evenly: four each.
        conf.setNumWorkers(3)
        StormSubmitter.submitTopology("exclamation-topology", conf, builder.createTopology())
    }

    class ExclamationBolt : BaseRichBolt() {
        var _collector: OutputCollector? = null
        override fun prepare(
            topoConf: MutableMap<String, Any>,
            topologyContext: TopologyContext?,
            collector: OutputCollector?
        ) {
            _collector = collector
        }

        override fun execute(tuple: Tuple) {
            // get the column word from tuple
            val word: String = tuple.getString(0)

            // build the word with the exclamation marks appended
            val exclamatedWord = StringBuilder()
            exclamatedWord.append(word).append("!!!")

            // emit the word with exclamations
            _collector?.emit(tuple, Values(exclamatedWord.toString()))
            _collector?.ack(tuple)
        }

        override fun declareOutputFields(declarer: OutputFieldsDeclarer) {
            // Tell storm the schema of the output tuple for this spout.
            // The tuple consists of a single column called 'exclamated-word'
            declarer.declare(Fields("exclamated-word"))
        }
    }
}