package cn.llonvne.entity.problem

import cn.llonvne.entity.DescriptionGetter
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * [Code] 的评论
 */
@Serializable
data class ShareCodeComment(
    val commentId: Int? = null,
    val committerAuthenticationUserId: Int,
    val shareCodeId: Int,
    val content: String,
    val type: ShareCodeCommentType = ShareCodeCommentType.Public,
    // --- 数据库信息区 ---//
    val version: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    companion object {
        @Serializable
        enum class ShareCodeCommentType : DescriptionGetter {
            Deleted,
            Public,
            Private,
            ;

            override val decr: String
                get() =
                    when (this) {
                        Deleted -> "被删除"
                        Public -> "公开的评论"
                        Private -> "私有的"
                    }
            override val reprName: String
                get() =
                    when (this) {
                        Deleted -> "被删除"
                        Public -> "公开"
                        Private -> "私密"
                    }
        }
    }
}
