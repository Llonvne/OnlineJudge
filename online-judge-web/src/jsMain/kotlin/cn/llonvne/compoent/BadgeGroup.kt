package cn.llonvne.compoent

import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.entity.types.badge.BadgeColorGetter
import cn.llonvne.entity.types.badge.cssClass
import io.kvision.core.Container
import io.kvision.html.Span
import io.kvision.html.span
import io.kvision.state.ObservableValue
import io.kvision.state.bind

fun <E : BadgeColorGetter> Container.badgeGroup(
    lst: Collection<E>,
    display: Span.(E) -> Unit,
) {
    span {
        lst.forEach { tag ->
            span {
                display(tag)

                addCssClass("badge")
                addCssClass("p-2")
                addCssClass("border")

                addCssClass(
                    tag.color.cssClass,
                )
            }
        }
    }
}

fun Container.badge(
    badgeColor: BadgeColor,
    display: Span.() -> Unit,
) = span {
    display()

    addCssClass("badge")
    addCssClass("p-2")
    addCssClass("border")

    addCssClass(
        badgeColor.cssClass,
    )
}

interface BadgesDsl {
    fun add(
        color: BadgeColor? = null,
        action: Span.() -> Unit,
    )

    fun <V> addBind(
        observableValue: ObservableValue<V>,
        action: Span.(V) -> Unit,
    )
}

private class BadgesImpl(
    private val target: Container,
) : BadgesDsl {
    private var index = 0

    override fun add(
        color: BadgeColor?,
        action: Span.() -> Unit,
    ) {
        target.badge(color ?: BadgeColor.entries[index++ % BadgeColor.entries.size]) {
            action()
        }
    }

    override fun <V> addBind(
        observableValue: ObservableValue<V>,
        action: Span.(V) -> Unit,
    ) {
        target.badge(BadgeColor.entries[index++ % BadgeColor.entries.size]) {}.bind(observableValue) {
            action(it)
        }
    }
}

fun Container.badges(action: BadgesDsl.() -> Unit) {
    BadgesImpl(this).action()
}
