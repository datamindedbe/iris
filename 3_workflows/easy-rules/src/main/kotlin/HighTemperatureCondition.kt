import org.jeasy.rules.api.Condition
import org.jeasy.rules.api.Facts

class HighTemperatureCondition : Condition {
    override fun evaluate(facts: Facts): Boolean {
        val temperature: Int = facts.get("temperature")
        return temperature > 25
    }

    companion object {
        fun itIsHot(): HighTemperatureCondition {
            return HighTemperatureCondition()
        }
    }
}