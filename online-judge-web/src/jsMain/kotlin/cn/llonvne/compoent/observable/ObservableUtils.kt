package cn.llonvne.compoent.observable

import cn.llonvne.AppScope
import cn.llonvne.compoent.loading
import io.kvision.core.Container
import io.kvision.state.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.experimental.ExperimentalTypeInference

data class ObservableDsl<V>(
    private val obv: ObservableValue<V?>,
    private var updater: suspend () -> V? = { null },
) : ObservableState<V?> {
    fun setUpdater(updater: suspend () -> V?) {
        this.updater = updater
    }

    fun getUpdater() = updater

    fun setObv(value: V?) {
        obv.value = value
    }

    fun <T : Container> T.sync(action: T.(V?) -> Unit): T {
        this.bind(this@ObservableDsl) {
            action(it)
        }
        return this
    }

    fun <T : Container> sync(
        container: T,
        action: T.(V?) -> Unit,
    ): T {
        container.sync(action)
        return container
    }

    fun Container.syncNotNull(action: Container.(V) -> Unit) {
        this.bind(this@ObservableDsl) {
            if (it != null) {
                action(it)
            }
        }
    }

    fun <T : Container> syncNotNull(
        container: T,
        onNull: T.() -> Unit = { loading() },
        action: T.(V) -> Unit,
    ) {
        container.sync { value ->
            if (value != null) {
                action(value)
            } else {
                onNull()
            }
        }
    }

    override fun getState(): V? = obv.getState()

    override fun subscribe(observer: (V?) -> Unit): () -> Unit = obv.subscribe(observer)
}

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
fun <V> observableOf(
    initialValue: V?,
    updater: suspend () -> V? = { null },
    coroutineScope: CoroutineScope = AppScope,
    action: ObservableDsl<V>.() -> Unit,
): ObservableDsl<V> {
    val observableDsl =
        ObservableDsl(
            obv = ObservableValue(initialValue),
            updater = updater,
        )

    action(observableDsl)

    coroutineScope.launch {
        observableDsl.setObv(observableDsl.getUpdater().invoke())
    }

    return observableDsl
}

data class ObservableListDsl<V>(
    private val obvListWrapper: ObservableListWrapper<V>,
    private var updater: suspend () -> List<V> = { emptyList() },
    private val coroutineScope: CoroutineScope,
) : ObservableList<V> by obvListWrapper,
    ObservableState<List<V>> by obvListWrapper {
    fun setUpdater(updater: suspend () -> List<V>) {
        this.updater = updater
    }

    fun updateList(updater: suspend () -> List<V>) {
        coroutineScope.launch {
            obvListWrapper.clear()
            obvListWrapper.addAll(updater.invoke())
        }
    }

    fun setObList(value: List<V>) {
        obvListWrapper.clear()
        obvListWrapper.addAll(value)
    }

    fun getUpdater() = updater

    override fun getState(): List<V> = obvListWrapper

    override fun subscribe(observer: (List<V>) -> Unit): () -> Unit {
        obvListWrapper.onUpdate += observer
        observer(this)
        return {
            obvListWrapper.onUpdate -= observer
        }
    }
}

fun <V> observableListOf(
    initialValue: MutableList<V> = mutableListOf(),
    updater: suspend () -> List<V> = { emptyList<V>() },
    coroutineScope: CoroutineScope = AppScope,
    action: ObservableListDsl<V>.() -> Unit,
) {
    val observableListDsl =
        ObservableListDsl(
            ObservableListWrapper(initialValue),
            updater,
            coroutineScope,
        )
    action(observableListDsl)

    coroutineScope.launch {
        observableListDsl.setObList(observableListDsl.getUpdater().invoke())
    }
}
