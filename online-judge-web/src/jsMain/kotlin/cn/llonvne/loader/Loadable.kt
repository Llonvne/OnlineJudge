package cn.llonvne.loader

import cn.llonvne.AppScope
import io.kvision.state.ObservableValue
import kotlinx.coroutines.launch
import kotlin.experimental.ExperimentalTypeInference

interface Loadable<Data> {
    suspend fun load(): ObservableValue<Data?>

    companion object {
        @OptIn(ExperimentalTypeInference::class)
        @OverloadResolutionByLambdaReturnType
        fun <Data : Any> load(loader: suspend () -> Data): Loadable<Data> = LoadableImpl(loader)
    }
}

private class LoadableImpl<Data : Any>(
    private val loader: suspend () -> Data,
) : Loadable<Data> {
    private val observableData = ObservableValue<Data?>(null)

    override suspend fun load(): ObservableValue<Data?> {
        AppScope.launch {
            observableData.value = loader.invoke()
        }
        return observableData
    }
}
