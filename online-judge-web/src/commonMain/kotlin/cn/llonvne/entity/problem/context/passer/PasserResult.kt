package cn.llonvne.entity.problem.context.passer

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
sealed interface PasserResult {
    @JvmInline
    @Serializable
    value class BooleanResult(val boolean: Boolean) : PasserResult
}