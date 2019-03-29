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

    val idCounts = HashMap<String, Int>()

    handleResult(idCounts, result)

    idCounts.entries
        .sortedBy { it.key }
        .forEach { println("${it.key} -> \t\t ${it.value}") }
}

fun handleResult(idCounts:HashMap<String, Int>, result: String) = runBlocking {


    println(result)

    val regex = "\\<img.*src=\"(.*?)\".*\\>"

    val matches = Regex(regex).findAll(result)

    println("matches: ${matches.count()}")
    println("matches: $matches.")
    matches.map { it.groupValues[1] }
        .filter { it.startsWith("https") }
        .forEachIndexed { index, url -> download(index, url, idCounts) }

}

fun CoroutineScope.download(
    index: Int,
    url: String,
    idCounts: HashMap<String, Int>
) {
    launch {
        withContext(Dispatchers.Default) {
            val threadId1 = "Default-${Thread.currentThread().id}"
            addCount(idCounts, index, threadId1)

            val imageBytes = getImage(url)


            withContext(Dispatchers.IO) {
                writeToFile(imageBytes)

                val threadId2 = "IO-${Thread.currentThread().id}"
                addCount(idCounts, index, threadId2)
            }
        }
    }
}


fun addCount(ids: HashMap<String, Int>, index:Int, threadId1:String){
    println("Download  $index \ton $threadId1")
    ids[threadId1] = (ids[threadId1]?:0)+1
}

fun writeToFile(imageBytes: ByteArray) {
    Files.write(Paths.get("./downloads/${Random.nextInt()}.jpg"), imageBytes)
}


fun getImage(url: String): ByteArray {
    return URL(url).readBytes()
}