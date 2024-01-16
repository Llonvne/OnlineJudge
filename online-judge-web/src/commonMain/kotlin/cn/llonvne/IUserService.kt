package cn.llonvne

import cn.llonvne.entity.AuthenticationUser
import io.kvision.annotations.KVService

@KVService
interface IUserService {
    suspend fun byId(id: Int): AuthenticationUser
}