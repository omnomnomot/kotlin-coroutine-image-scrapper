import kotlinx.coroutines.*
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.random.Random

fun main(args: Array<String>) {
    val spec =
        "https://stock.adobe.com/ie/collections/Kqy5H45lnKzkC5I8JGFXYVwr2ltNwMe7?filters%5Bcontent_type%3Aphoto%5D=1"
    val client = URL(spec)

    val result = client.readText()

    handleResult(result)
}

fun handleResult(result: String) = runBlocking {
    println(result)

    val regex = "\\<img.*src=\"(.*?)\".*\\>"

    val matches = Regex(regex).findAll(result)

    println("matches: ${matches.count()}")
    println("matches: $matches.")
    matches.map { it.groupValues[1] }
        .filter { it.startsWith("https") }
        .forEach {
            download(it)
        }

}

fun CoroutineScope.download(url: String) {

    launch {
        withContext(Dispatchers.Default) {
            val imageBytes = getImage(url)
            withContext(Dispatchers.IO) {
                writeToFile(imageBytes)
            }
        }
    }

}

fun writeToFile(imageBytes: ByteArray) {
    Files.write(Paths.get("./downloads/${Random.nextInt()}.jpg"), imageBytes)
}


fun getImage(url: String): ByteArray {
    return URL(url).readBytes()
}