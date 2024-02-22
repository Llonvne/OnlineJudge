package cn.llonvne.entity.problem.context;

import kotlinx.serialization.Serializable

@Serializable
enum class TestCaseType {
    OnlyForView,
    OnlyForJudge,
    ViewAndJudge,
    Deprecated,
    Building;

    val chinese
        get() = when (this) {
            OnlyForView -> "仅展示"
            OnlyForJudge -> "仅判题"
            ViewAndJudge -> "展示/判题"
            Deprecated -> "废弃"
            Building -> "建设中"
        }
}