import HighTemperatureCondition.Companion.itIsHot
import WorkflowAction.Companion.triggerWorkflow
import org.apache.log4j.BasicConfigurator
import org.jeasy.rules.api.Facts
import org.jeasy.rules.api.Rule
import org.jeasy.rules.api.Rules
import org.jeasy.rules.api.RulesEngine
import org.jeasy.rules.core.InferenceRulesEngine
import org.jeasy.rules.core.RuleBuilder

fun main(args: Array<String>) {
    BasicConfigurator.configure();

    // define facts
    val facts = Facts()
    facts.put("temperature", 30)

    // define rules
    val workflowRule: Rule = RuleBuilder()
        .name("workflow rule")
        .`when`(itIsHot())
        .then(triggerWorkflow())
        .build()
    val rules = Rules(workflowRule)
    val rulesEngine: RulesEngine = InferenceRulesEngine()
    rulesEngine.fire(rules, facts)
}
