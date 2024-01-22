package cn.llonvne.entity.problem.share

import cn.llonvne.entity.problem.share.CodeVisibilityType.*
import kotlinx.serialization.Serializable

@Serializable
enum class CodeVisibilityType {
    Public, Private, Restrict
}

val CodeVisibilityType.decr: String
    get() = when (this) {
        Public -> "对所有人可见"
        Private -> "仅对自己可见"
        Restrict -> "对特定的链接可见（通过 ID 访问将显示为不可见）"
    }

val CodeVisibilityType.chinese: String
    get() = when (this) {
        Public -> "公开"
        Private -> "私有"
        Restrict -> "受限"
    }


