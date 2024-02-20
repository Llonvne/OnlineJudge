package cn.llonvne.kvision.service

import cn.llonvne.database.repository.GroupRepository
import cn.llonvne.database.resolver.GroupIdResolver
import cn.llonvne.database.resolver.GroupLoadResolver
import cn.llonvne.database.resolver.JoinGroupResolver
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.role.CreateGroup
import cn.llonvne.entity.role.GroupManager
import cn.llonvne.getLogger
import cn.llonvne.kvision.service.IGroupService.*
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
    private val authentication: RedisAuthenticationService,
    private val groupRepository: GroupRepository,
    private val roleService: RoleService,
    private val groupIdResolver: GroupIdResolver,
    private val groupLoadResolver: GroupLoadResolver,
    private val joinGroupResolver: JoinGroupResolver
) : IGroupService {

    private val logger = getLogger()

    override suspend fun createTeam(
        authenticationToken: AuthenticationToken, createGroupReq: CreateGroupReq
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
        authenticationToken: AuthenticationToken?, groupId: GroupId
    ): LoadGroupResp = logger.track(authenticationToken, groupId) {
        val id = groupIdResolver.resolve(groupId) ?: return@track GroupIdNotFound(groupId)

        val user = authentication.getAuthenticationUser(authenticationToken)

        return@track groupLoadResolver.resolve(groupId, id, user)
    }

    override suspend fun join(groupId: GroupId, authenticationToken: AuthenticationToken): JoinGroupResp {
        val id = groupIdResolver.resolve(groupId) ?: return GroupIdNotFound(groupId)

        val user = authentication.validate(authenticationToken) { requireLogin() }

        if (user == null) {
            return PermissionDenied
        }

        return joinGroupResolver.resolve(groupId, id, user)
    }
}