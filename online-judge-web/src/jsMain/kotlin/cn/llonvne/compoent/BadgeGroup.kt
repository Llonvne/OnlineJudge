package cn.llonvne.compoent

import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.entity.types.badge.BadgeColor.*
import cn.llonvne.entity.types.badge.BadgeColorGetter
import cn.llonvne.entity.types.badge.cssClass
import io.kvision.core.Container
import io.kvision.html.Span
import io.kvision.html.span


fun <E : BadgeColorGetter> Container.badgeGroup(
    lst: Collection<E>,
    display: Span.(E) -> Unit
) {
    span {
        lst.forEach { tag ->
            span {
                display(tag)

                addCssClass("badge")
                addCssClass("p-2")
                addCssClass("border")

                addCssClass(
                    tag.color.cssClass
                )
            }
        }
    }
}

fun Container.badge(badgeColor: BadgeColor, display: Span.() -> Unit) = span {
    display()

    addCssClass("badge")
    addCssClass("p-2")
    addCssClass("border")

    addCssClass(
        badgeColor.cssClass
    )
}