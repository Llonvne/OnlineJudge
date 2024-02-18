package cn.llonvne.database.entity.def

import cn.llonvne.entity.group.Group
import cn.llonvne.entity.group.GroupType
import cn.llonvne.entity.group.GroupVisibility
import kotlinx.datetime.LocalDateTime
import org.komapper.annotation.*

@KomapperEntityDef(entity = Group::class)
@KomapperTable(name = "_group")
/**
 * group 被 PostgresSQL 作为关键字故更改为 _group
 * [cn.llonvne.entity.group.Group]
 */
private data class GroupDef(
    @KomapperId
    @KomapperAutoIncrement
    val groupId: Int? = null,
    val groupName: String,
    val groupShortName: String,
    val groupHash: String,
    val visibility: GroupVisibility,
    val type: GroupType,
    @KomapperVersion
    val version: Int? = null,
    @KomapperCreatedAt
    val createdAt: LocalDateTime? = null,
    @KomapperUpdatedAt
    val updatedAt: LocalDateTime? = null
)