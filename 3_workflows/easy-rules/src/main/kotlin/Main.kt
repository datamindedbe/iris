import HighTemperatureCondition.Companion.itIsHot
import org.apache.log4j.BasicConfigurator
import org.jeasy.rules.api.Facts
import org.jeasy.rules.api.Rule
import org.jeasy.rules.api.Rules
import org.jeasy.rules.api.RulesEngine
import org.jeasy.rules.core.InferenceRulesEngine
import org.jeasy.rules.core.RuleBuilder
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import java.nio.charset.StandardCharsets

fun main(args: Array<String>) {
    BasicConfigurator.configure();

    // define facts
    val facts = Facts()
    facts.put("temperature", 30)

    Thread(Runnable {
        updateTemp(facts)
    }).start();

    // define rules
//    val workflowRule: Rule = RuleBuilder()
//        .name("workflow rule")
//        .`when`(itIsHot())
//        .then(triggerSequenceWorkflow())
//        .`else`(triggerParallelWorkflow())
//        .build()
//    val rules = Rules(workflowRule)
    val rules: Rules  = Rules(HotRule(), ColdRule())
//    rules.register(Hotrule())
//    rules.register(Coldrule())
    val rulesEngine: RulesEngine = InferenceRulesEngine()
    rulesEngine.fire(rules, facts)
}

fun updateTemp(temp: Facts) {
    val factory = ConnectionFactory()
    val connection = factory.newConnection("amqp://guest:guest@localhost:5672/")
    val channel = connection.createChannel()
    val consumerTag = "SimpleConsumer"
    val queue_name = "temp-queue"

    channel.queueDeclare(queue_name, false, false, false, null)

    println("[$consumerTag] Waiting for messages...")
    val deliverCallback = DeliverCallback { consumerTag: String?, delivery: Delivery ->
        val message = String(delivery.body, StandardCharsets.UTF_8)
        println("[$consumerTag] Received temp: '$message'")
        temp.put("temperature", Integer.parseInt(message))
    }
    val cancelCallback = CancelCallback { consumerTag: String? ->
        println("[$consumerTag] was canceled")
    }

    channel.basicConsume(queue_name, true, consumerTag, deliverCallback, cancelCallback)
}
