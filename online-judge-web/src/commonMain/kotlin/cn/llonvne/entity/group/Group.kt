package cn.llonvne.entity.group

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val groupId: Int? = null,
    val groupName: String,
    val groupShortName: String,
    val groupHash: String,
    val description: String,
    val visibility: GroupVisibility,
    val type: GroupType,
    val version: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)





