package cn.llonvne.database.resolver.contest

import cn.llonvne.database.repository.ContestRepository
import cn.llonvne.entity.contest.Contest
import cn.llonvne.entity.contest.ContestId
import cn.llonvne.entity.contest.HashId
import cn.llonvne.entity.contest.IntId
import org.springframework.stereotype.Service

@Service
class ContestIdGetResolver(
    private val contestRepository: ContestRepository,
) {
    suspend fun resolve(id: ContestId): Contest? =
        when (id) {
            is HashId -> contestRepository.getByHash(id.hash)
            is IntId -> contestRepository.getById(id.id)
        }
}
