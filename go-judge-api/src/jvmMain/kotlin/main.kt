import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

suspend fun main() {
    a()
        .buffer(3, BufferOverflow.SUSPEND)
        .map { it * it }
        .filter { it % 2 == 1 }
        .collect {
            println(it)
        }
}

fun a() = flow {
    (1..10000)
        .forEach {
            emit(it)
        }
}