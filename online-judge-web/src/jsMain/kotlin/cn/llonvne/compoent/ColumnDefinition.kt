package cn.llonvne.compoent

import io.kvision.core.Component
import io.kvision.core.Container
import io.kvision.tabulator.ColumnDefinition


fun <T : Any> Container.defineColumn(title: String, format: ((data: T) -> Component)): ColumnDefinition<T> {
    return ColumnDefinition(title, formatterComponentFunction = { _, _, e ->
        format(e)
    })
}