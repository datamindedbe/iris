import org.jeasy.rules.api.Facts
import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.Thread.sleep
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

import org.jeasy.rules.annotation.Action
import org.jeasy.rules.annotation.Condition
import org.jeasy.rules.annotation.Fact
import org.jeasy.rules.annotation.Priority
import org.jeasy.rules.annotation.Rule

@Rule
class HotRule {
    @Condition
    fun isHot(@Fact("temperature") number: Integer): Boolean {
        return number > 15
    }

    @Action
    fun triggerParallelWorkflow() {

        val values = mapOf("conf" to {})

        val objectMapper = ObjectMapper()
        val requestBody: String = objectMapper
            .writeValueAsString(values)

        val auth = Base64.getEncoder().encode(("airflow" + ":" + "airflow")
            .toByteArray()).toString(Charsets.UTF_8)
        val client = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/api/v1/dags/Parallel/dagRuns"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Basic $auth")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString());
        println(response.body())
        sleep(1000)
    }

    @get:Priority
    val priority: Int
        get() = 2
}