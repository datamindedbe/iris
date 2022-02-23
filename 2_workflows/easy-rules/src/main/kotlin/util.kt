import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

fun triggerWorkflow(dag: String) {
    val values = mapOf("conf" to {})

    val objectMapper = ObjectMapper()
    val requestBody: String = objectMapper
        .writeValueAsString(values)

    val auth = Base64.getEncoder().encode(("airflow" + ":" + "airflow")
        .toByteArray()).toString(Charsets.UTF_8)
    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8080/api/v1/dags/$dag/dagRuns"))
        .header("Content-Type", "application/json")
        .header("Authorization", "Basic $auth")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString());
    println(response.body())
}
