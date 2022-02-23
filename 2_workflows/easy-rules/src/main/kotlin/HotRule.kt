import org.jeasy.rules.api.Facts

import java.lang.Thread.sleep

import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Priority
import org.jeasy.rules.annotation.Rule

@Rule
class HotRule {
    @Condition
    fun isHot(@Fact("temperature") number: kotlin.Int): Boolean {
        return number > 15
    }

    @Action
    fun triggerParallelWorkflow() {
        triggerWorkflow("Parallel")
        sleep(1000)
    }

    @get:Priority
    val priority: Int
        get() = 3
}