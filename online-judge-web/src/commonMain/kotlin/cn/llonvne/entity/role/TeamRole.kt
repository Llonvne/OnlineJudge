@file:UseContextualSerialization

package cn.llonvne.entity.role

import cn.llonvne.entity.group.GroupType
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.reflect.KClass


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
            return TeamRole.withSimpleName(CreateTeam::class) {
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







