import client.FileClient
import io.ktor.client.call.NoTransformationFoundException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.tinkoff.client.KudaGoClient
import org.tinkoff.dto.News
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val kudaGoClient = KudaGoClient()
    val fileClient = FileClient();
    val countOfThreads: Int = 10
    val executor = Executors.newFixedThreadPool(countOfThreads)

    fileClient.clearFile("src/main/resources/news.csv")

    val channel = Channel<List<News>>()

    val scope = CoroutineScope(Dispatchers.Default)
    val tasks = (1..countOfThreads).map { i ->
        scope.launch {
            try {
                for (page in 1..20) {
                    if (page % countOfThreads == i - 1) {
                        val requestContent = kudaGoClient.getNews(page = page)

                        if (!requestContent.isEmpty()) channel.send(requestContent)
                    }
                }
            } catch (e: NoTransformationFoundException) {
                println("Error: ${e.message}")
            }
        }
    }

    val readerJob = scope.launch {
        while (true) {
            val result = channel.receiveCatching().getOrNull()
            if (result != null) {
                fileClient.saveNews("src/main/resources/news.csv", result)
            } else {
                break
            }
        }
    }

    runBlocking {
        tasks.forEach { it.join() }
        channel.close()
        readerJob.join()
    }

    executor.shutdown()
    executor.awaitTermination(1, TimeUnit.HOURS)
    println("Finished all threads")
}
