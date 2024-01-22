package cn.llonvne.entity.problem.share

enum class CodeCommentType {
    Open,
    Closed,
    ClosedByAdmin,
    Protected
}

fun CodeCommentType.decr() = when (this) {
    CodeCommentType.Open -> "任何人均可查看，均可发表评论"
    CodeCommentType.Closed -> "评论区被关闭"
    CodeCommentType.ClosedByAdmin -> "评论区被管理员关闭"
    CodeCommentType.Protected -> "评论区被保护（需经过审核才能公开展示）"
}