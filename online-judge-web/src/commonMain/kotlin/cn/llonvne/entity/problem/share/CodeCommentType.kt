package cn.llonvne.entity.problem.share

import cn.llonvne.entity.DescriptionGetter
import cn.llonvne.security.AuthenticationToken
import kotlinx.serialization.Serializable
import kotlin.enums.EnumEntries

@Serializable
enum class CodeCommentType : DescriptionGetter {
    Open,
    Closed,
    ClosedByAdmin,
    Protected;

    override val decr: String
        get() = decr()
    override val reprName: String
        get() = name
}

fun CodeCommentType.decr() = when (this) {
    CodeCommentType.Open -> "任何人均可查看，均可发表评论"
    CodeCommentType.Closed -> "评论区被关闭"
    CodeCommentType.ClosedByAdmin -> "评论区被管理员关闭"
    CodeCommentType.Protected -> "评论区被保护（需经过审核才能公开展示）"
}

fun EnumEntries<CodeCommentType>.limited(token: AuthenticationToken): EnumEntries<CodeCommentType> {
    return this
}