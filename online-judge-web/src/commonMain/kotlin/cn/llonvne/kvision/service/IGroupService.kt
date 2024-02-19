package cn.llonvne.kvision.service

import cn.llonvne.entity.group.Group
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.group.GroupType
import cn.llonvne.entity.group.GroupVisibility
import cn.llonvne.kvision.service.Validatable.Companion.on
import cn.llonvne.kvision.service.Validatable.Companion.validate
import cn.llonvne.security.AuthenticationToken
import io.kvision.annotations.KVService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@KVService
interface IGroupService {
    @Serializable
    data class CreateGroupReq(
        val groupName: String, val groupShortName: String, val teamVisibility: GroupVisibility, val groupType: GroupType
    ) : Validatable {
        override fun validate() = validate {
            on(groupName, "队伍名称必须在 6..100 之间") {
                length in 6..100
            }

            on(groupShortName, "短名称必须在 3..20之间") {
                length in 3..20
            }
        }
    }

    @Serializable
    sealed interface CreateGroupResp {
        @Serializable
        data class CreateGroupOk(val group: Group) : CreateGroupResp
    }

    suspend fun createTeam(
        authenticationToken: AuthenticationToken, createGroupReq: CreateGroupReq
    ): CreateGroupResp

    @Serializable
    sealed interface LoadGroupResp {
        @Serializable
        data class GuestLoadGroup(
            val groupName: String,
            val groupShortName: String,
            val visibility: GroupVisibility,
            @SerialName("groupType")
            val type: GroupType,
            val ownerName: String
        ) : LoadGroupResp
    }

    suspend fun load(authenticationToken: AuthenticationToken?, groupId: GroupId): LoadGroupResp
}