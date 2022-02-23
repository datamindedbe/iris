import org.jeasy.rules.api.Facts
import java.lang.Thread.sleep

import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Priority
import org.jeasy.rules.annotation.Rule

@Rule
class ColdRule {
    @Condition
    fun isCold(@Fact("temperature") number: Integer): Boolean {
        return number <= 15
    }

    @Action
    fun triggerSequenceWorkflow() {
        triggerWorkflow("Sequence")
        sleep(1000)
    }

    @get:Priority
    val priority: Int
        get() = 2
}