package cn.llonvne.entity.group

sealed interface GroupId {
    data class IntGroupId(val id: Int) : GroupId

    data class HashGroupId(val id: String) : GroupId

    data class ShortGroupName(val shortName: String) : GroupId
}

