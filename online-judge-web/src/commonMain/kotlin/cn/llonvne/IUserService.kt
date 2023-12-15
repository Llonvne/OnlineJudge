package cn.llonvne

import cn.llonvne.entity.User
import io.kvision.annotations.KVService

@KVService
interface IUserService {
    suspend fun byId(id: Int): User
}