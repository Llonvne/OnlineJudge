package cn.llonvne.compoent

import cn.llonvne.entity.types.BadgeColor
import cn.llonvne.entity.types.BadgeColorGetter
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
                    when (tag.color) {
                        BadgeColor.Green -> "bg-success"
                        BadgeColor.Red -> "bg-danger"
                        BadgeColor.Blue -> "bg-primary"
                    }
                )
            }
        }
    }
}