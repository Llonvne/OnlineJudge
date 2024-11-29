package cn.llonvne.entity.group

import kotlinx.serialization.Serializable

@Serializable
sealed interface GroupId {
    val path: String

    @Serializable
    data class IntGroupId(
        val id: Int,
    ) : GroupId {
        override fun toString(): String = "<id-$id>"

        override val path: String
            get() = id.toString()
    }

    @Serializable
    data class HashGroupId(
        val id: String,
    ) : GroupId {
        override fun toString(): String = "<hash-$id>"

        override val path: String
            get() = id
    }

    @Serializable
    data class ShortGroupName(
        val shortName: String,
    ) : GroupId {
        override fun toString(): String = "short-$shortName"

        override val path: String
            get() = shortName
    }
}
