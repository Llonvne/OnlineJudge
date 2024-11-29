package cn.llonvne.entity.problem.context

import kotlinx.serialization.Serializable

@Serializable
enum class ProblemVisibility {
    Public,
    Private,
    Restrict,
    ;

    val chinese
        get() =
            when (this) {
                Public -> "公开的题目,所有人都可以查看，所有人都可以提交"
                Private -> "私有的题目，任何人都无法查看"
                Restrict -> "受限制的题目，只能通过特定的Hash链接访问，并提交"
            }
}
