package cn.llonvne.entity.group

import kotlinx.serialization.Serializable

@Serializable
sealed interface GroupId {
    @Serializable
    data class IntGroupId(val id: Int) : GroupId {
        override fun toString(): String {
            return "<id-$id>"
        }
    }

    @Serializable
    data class HashGroupId(val id: String) : GroupId {
        override fun toString(): String {
            return "<hash-$id>"
        }
    }

    @Serializable
    data class ShortGroupName(val shortName: String) : GroupId{
        override fun toString(): String {
            return "short-$shortName"
        }
    }
}

