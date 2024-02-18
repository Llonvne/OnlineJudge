package cn.llonvne.entity.group

import kotlinx.serialization.Serializable

@Serializable
enum class GroupType {
    Classic,
    College,
    Team;

    val chinese
        get() = when (this) {
            Classic -> "经典的小组，拥有经典的组织层次(管理员-成员结构)，该选项面对所有用户开放"
            College -> "学校，拥有类学校的组织层次(管理员-教师-助教-学生-...),该选项需要验证您的身份"
            Team -> "学校附属的小组结构，不拥有管理员（管理权由对应的学校拥有）"
        }

    companion object {
        val options: List<Pair<String, String>> = entries.map {
            it.ordinal.toString() to it.chinese
        }
        val defaultOption get() = Classic.ordinal.toString() to Classic.chinese
    }
}