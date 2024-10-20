package client

import org.slf4j.LoggerFactory
import org.tinkoff.dto.News
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class FileClient {
    companion object {
        private val logger = LoggerFactory.getLogger("FileClient")
    }

    fun saveNews(path: String, news: Collection<News>) {
        val filePath = Paths.get(path)

        if (!Files.exists(filePath)) {
            Files.createFile(filePath)
            logger.info("Created new file at $path")
        } else {
            logger.warn("File already exists at $path")
        }

        val csv = news.joinToString("\n") { news ->
            "\"${news.id}\",\"${news.title}\",\"${news.publicationDate}\",\"${news.slug}\",\"${news.place}\",\"${news.description}\",\"${news.siteUrl}\",\"${news.favoritesCount}\",\"${news.commentsCount}\",\"${news.rating}\""
        }

        File(path).appendText(csv + "\n")
        logger.info("Appended ${news.size} news items to $path")
    }

    fun clearFile(path: String) {
        val filePath = Paths.get(path)

        if (Files.exists(filePath)) {
            File(path).writeText("")
            logger.info("Cleared the file at $path")
        } else {
            logger.warn("File does not exist at $path, nothing to clear")
        }
    }
}
