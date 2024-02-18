package cn.llonvne.database.repository

import cn.llonvne.database.entity.def.group
import cn.llonvne.entity.group.Group
import cn.llonvne.kvision.service.GroupHashService
import cn.llonvne.kvision.service.IGroupService.CreateGroupReq
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.query.map
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Repository

@Repository
class GroupRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase,
    private val groupHashService: GroupHashService
) {

    private val groupMeta = Meta.group

    suspend fun create(createGroupReq: CreateGroupReq): Group {
        return db.runQuery {
            QueryDsl.insert(groupMeta).single(
                Group(
                    groupName = createGroupReq.groupName,
                    groupShortName = createGroupReq.groupShortName,
                    groupHash = groupHashService.hash(),
                    visibility = createGroupReq.teamVisibility,
                    type = createGroupReq.groupType
                )
            ).returning()
        }
    }

    suspend fun shortNameAvailable(name: String): Boolean {
        return db.runQuery {
            QueryDsl.from(groupMeta).where {
                groupMeta.groupShortName eq name
            }.select(count())
                .map { it == 0.toLong() }
        }
    }

}
