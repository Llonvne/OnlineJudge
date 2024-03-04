package cn.llonvne.entity.problem.context.passer

import cn.llonvne.entity.types.badge.BadgeColor
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
sealed interface PasserResult {

    val readable: String

    @Serializable
    class BooleanResult(
        val result: Boolean
    ) : PasserResult {
        val suggestColor: BadgeColor
            get() = when (result) {
                true -> BadgeColor.Green
                false -> BadgeColor.Red
            }
        override val readable
            get() = when (result) {
                true -> "Accepted"
                false -> "Wrong Answer"
            }
    }


}