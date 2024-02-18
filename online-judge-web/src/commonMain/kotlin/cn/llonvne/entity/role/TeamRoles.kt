@file:UseContextualSerialization

package cn.llonvne.entity.role

import cn.llonvne.entity.group.GroupType
import cn.llonvne.entity.role.TeamIdRole.Companion.simpleName
import cn.llonvne.entity.role.TeamRole.Companion.withSimpleName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.reflect.KClass

/**
 * 权限认证接口
 */
@Serializable
sealed interface Role {
    /**
     * 检查 [provide] 是否可以通过认证
     */
    fun check(provide: Role): Boolean
}

/**
 * 特定队伍权限身份接口，表示该权限对于唯一一个 [teamId]
 */
@Serializable
sealed interface TeamIdRole : Role {
    /**
     * [teamId] 标识符号
     */
    val teamId: Int

    companion object {
        fun TeamIdRole.simpleName(cls: KClass<*>): String {
            return "<${cls.simpleName}-id-$teamId>"
        }
    }
}

/**
 * 通用队伍权限身份接口，该接口用于表示**通用权限**
 */
@Serializable
sealed interface TeamRole : Role {
    companion object {
        fun default(): List<TeamRole> = listOf(CreateTeam.CreateTeamImpl())
        fun TeamRole.simpleName(cls: KClass<*>): String = withSimpleName(cls) { "" }

        fun withSimpleName(cls: KClass<*>, build: () -> String) = "<${cls.simpleName}-${build()}>"
    }
}

private inline fun <reified T : TeamIdRole> T.checkInternal(role: Role): Boolean {
    if (role is T) {
        return role.teamId == teamId
    }
    return false
}

@Serializable
sealed interface CreateTeam : TeamRole {

    val teamTypes: List<GroupType>

    @Serializable
    data class CreateTeamImpl(
        override val teamTypes: List<GroupType> = listOf(GroupType.Classic)
    ) : CreateTeam {
        override fun check(provide: Role): Boolean {
            return if (provide is CreateTeam) {
                return provide.teamTypes.containsAll(teamTypes)
            } else {
                false
            }
        }

        override fun toString(): String {
            return withSimpleName(CreateTeam::class) {
                teamTypes.joinToString(",") { it.name }
            }
        }
    }

    companion object {
        fun require(type: GroupType): CreateTeam {
            return CreateTeamImpl(listOf(type))
        }
    }
}

@Serializable
sealed interface DeleteTeam : TeamIdRole {
    @Serializable
    data class DeleteTeamImpl(override val teamId: Int) : DeleteTeam {
        override fun check(provide: Role): Boolean = checkInternal<DeleteTeam>(provide)
        override fun toString(): String {
            return simpleName(DeleteTeam::class)
        }
    }
}

@Serializable
sealed interface InviteMember : TeamIdRole {
    @Serializable
    data class InviteMemberImpl(override val teamId: Int) : InviteMember {
        override fun check(provide: Role): Boolean = checkInternal<InviteMember>(provide)
        override fun toString(): String {
            return simpleName(InviteMember::class)
        }
    }
}

@Serializable
sealed interface KickMember : TeamIdRole {
    @Serializable
    data class KickMemberImpl(override val teamId: Int) : KickMember {
        override fun check(provide: Role): Boolean = checkInternal<KickMember>(provide)
        override fun toString(): String {
            return simpleName(KickMember::class)
        }
    }
}

@Serializable
sealed interface TeamManager : DeleteTeam, InviteMember, KickMember {
    @Serializable
    data class TeamMangerImpl(override val teamId: Int) : TeamManager {
        override fun check(provide: Role): Boolean = checkInternal<TeamManager>(provide)
        override fun toString(): String {
            return simpleName(TeamManager::class)
        }
    }
}



