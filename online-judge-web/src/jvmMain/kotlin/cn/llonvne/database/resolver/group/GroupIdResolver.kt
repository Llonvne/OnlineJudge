package cn.llonvne.database.resolver.group

import cn.llonvne.database.repository.GroupRepository
import cn.llonvne.entity.group.GroupId
import org.springframework.stereotype.Service

/**
 * 将 [GroupId] 转换为 数字ID 并保证该ID存在，否则返回 null
 */
@Service
class GroupIdResolver(
    private val groupRepository: GroupRepository
) {

    suspend fun resolve(groupId: GroupId): Int? {
        val intId = when (groupId) {
            is GroupId.HashGroupId -> fromHashGroupId(groupId.id)
            is GroupId.IntGroupId -> groupId.id
            is GroupId.ShortGroupName -> fromShortName(groupId.shortName)
        }
        return validateGroupId(intId)
    }

    private suspend fun validateGroupId(id: Int?): Int? {
        if (id == null) {
            return null
        }
        return if (groupRepository.isIdExist(id)) {
            id
        } else {
            null
        }
    }

    private suspend fun fromHashGroupId(hash: String): Int? {
        return groupRepository.fromHashToId(hash)
    }

    private suspend fun fromShortName(shortname: String): Int? {
        return groupRepository.fromShortname(shortname)
    }
}