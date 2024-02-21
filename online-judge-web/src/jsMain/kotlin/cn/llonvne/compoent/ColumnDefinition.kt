package cn.llonvne.compoent

import io.kvision.core.Component
import io.kvision.tabulator.ColumnDefinition


fun <T : Any> defineColumn(title: String, format: (data: T) -> Component): ColumnDefinition<T> {
    return ColumnDefinition(title, formatterComponentFunction = { _, _, e ->
        format(e)
    })
}