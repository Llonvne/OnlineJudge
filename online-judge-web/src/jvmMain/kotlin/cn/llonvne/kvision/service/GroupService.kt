package cn.llonvne.kvision.service

import cn.llonvne.database.repository.AuthenticationUserRepository
import cn.llonvne.database.repository.GroupRepository
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.role.CreateGroup
import cn.llonvne.entity.role.GroupManager
import cn.llonvne.getLogger
import cn.llonvne.kvision.service.IGroupService.CreateGroupReq
import cn.llonvne.kvision.service.IGroupService.CreateGroupResp
import cn.llonvne.kvision.service.IGroupService.CreateGroupResp.CreateGroupOk
import cn.llonvne.security.AuthenticationToken
import cn.llonvne.security.RedisAuthenticationService
import cn.llonvne.track
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class GroupService(
    val authentication: RedisAuthenticationService,
    val groupRepository: GroupRepository,
    val authenticationUserRepository: AuthenticationUserRepository,
    val roleService: RoleService
) : IGroupService {

    private val logger = getLogger()

    override suspend fun createTeam(
        authenticationToken: AuthenticationToken,
        createGroupReq: CreateGroupReq
    ): CreateGroupResp {
        val user = authentication.validate(authenticationToken) {
            check(CreateGroup.require(createGroupReq.groupType))
        } ?: return PermissionDenied

        if (!groupRepository.shortNameAvailable(createGroupReq.groupShortName)) {
            return GroupShortNameUnavailable
        }

        return logger.track(authenticationToken, createGroupReq.groupName, createGroupReq.groupShortName) {
            info("$authenticationToken try to create $createGroupReq")
            val group = groupRepository.create(createGroupReq)
            info("$authenticationToken created ${createGroupReq.groupName},id is ${group.groupId}")

            roleService.addRole(
                user.id, GroupManager.GroupMangerImpl(
                    group.groupId ?: return@track InternalError("创建小组后仍然 id 为空")
                )
            )

            return@track CreateGroupOk(group)
        }
    }

    override suspend fun load(
        authenticationToken: AuthenticationToken?,
        groupId: GroupId
    ): IGroupService.LoadGroupResp = logger.track(authenticationToken, groupId) {
        when (groupId) {
            is GroupId.HashGroupId -> TODO()
            is GroupId.IntGroupId -> TODO()
            is GroupId.ShortGroupName -> TODO()
        }
    }
}