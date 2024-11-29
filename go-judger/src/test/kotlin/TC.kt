import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test

class TC {
    sealed interface Result {
        data class Running(
            val percent: Long,
        ) : Result

        data object Finished : Result
    }

    @Test
    fun a() =
        runBlocking {
            suspendCoroutine<Unit> { continuation ->
                println("start!")

                continuation.resume(Unit)
            }
            println("end")
        }
}
