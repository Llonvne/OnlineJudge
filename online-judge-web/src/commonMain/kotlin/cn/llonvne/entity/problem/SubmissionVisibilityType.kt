package cn.llonvne.entity.problem

enum class SubmissionVisibilityType {
    PUBLIC,
    PRIVATE,
    Contest,
    ;

    val chinese
        get() =
            when (this) {
                PUBLIC -> "公开"
                PRIVATE -> "私有"
                Contest -> "比赛"
            }
}
