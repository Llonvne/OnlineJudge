package cn.llonvne.entity.group;

import kotlinx.serialization.Serializable

@Serializable
enum class GroupVisibility {
    Public, Private, Restrict;

    val chinese: String
        get() = when (this) {
            Public -> "公开的小组，对任何人都可见，任何都可以自由加入"
            Private -> "非空开的小组，对任何人都不可见，任何人都无法加入"
            Restrict -> "公开的小组，加入者需要通过Hash或者由管理员审批"
        }

    val shortChinese get() = when(this){
        Public -> "公开"
        Private -> "私有"
        Restrict -> "受限制的"
    }

    companion object {
        val options: List<Pair<String, String>> = entries.map {
            it.ordinal.toString() to it.chinese
        }
    }
}