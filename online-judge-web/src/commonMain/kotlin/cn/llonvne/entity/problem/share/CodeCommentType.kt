package cn.llonvne.entity.problem.share

import cn.llonvne.entity.DescriptionGetter
import cn.llonvne.entity.problem.share.CodeCommentType.*
import cn.llonvne.security.Token
import kotlinx.serialization.Serializable
import kotlin.enums.EnumEntries

@Serializable
enum class CodeCommentType : DescriptionGetter {
    Open,
    Closed,
    ClosedByAdmin,
    Freezing,
    Protected,
    ContestCode,
    ;

    override val decr: String
        get() = decr()
    override val reprName: String
        get() = name
}

private fun CodeCommentType.decr() =
    when (this) {
        Open -> "任何人均可查看，均可发表评论"
        Closed -> "评论区被关闭"
        ClosedByAdmin -> "评论区被管理员关闭"
        Protected -> "评论区被保护（需经过审核才能公开展示）"
        Freezing -> "评论区被冻结，无法添加/删除/修改评论"
        ContestCode -> "比赛代码不支持评论"
    }

fun EnumEntries<CodeCommentType>.limited(token: Token): List<CodeCommentType> = this.filter { it != ContestCode }
