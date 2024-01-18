package cn.llonvne.model

import io.kvision.state.MutableState
import io.kvision.state.ObservableState
import io.kvision.state.ObservableValue
import kotlinx.browser.localStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object Storage {

    interface RememberObject<K> : ReadWriteProperty<Any?, K> {
        fun get(): K

        fun set(k: K)

        fun clear()
    }

    inline fun <reified K> remember(
        initial: K,
        key: String,
    ): RememberObject<K> {
        if (localStorage[key] == "" && localStorage[key] == null) {
            localStorage[key] = Json.encodeToString(initial)
        }
        return object : RememberObject<K> {

            private val observers = mutableListOf<(K) -> Unit>()

            override fun getValue(thisRef: Any?, property: KProperty<*>): K {
                return get()
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: K) {
                observers.forEach { it.invoke(value) }
                set(value)
            }

            override fun get(): K {
                val result = localStorage[key] ?: return initial
                return Json.decodeFromString(result)
            }

            override fun set(k: K) {
                if (k == null) {
                    clear()
                } else {
                    localStorage[key] = Json.encodeToString(k)
                }
            }

            override fun clear() {
                localStorage.removeItem(key)
            }
        }
    }
}