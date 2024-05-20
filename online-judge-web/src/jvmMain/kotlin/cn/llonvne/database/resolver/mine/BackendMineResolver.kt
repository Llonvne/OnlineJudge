package cn.llonvne.database.resolver.mine

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.kvision.service.IAuthenticationService
import org.springframework.stereotype.Service

@Service
class BackendMineResolver {
    suspend fun resolve(user: AuthenticationUser): IAuthenticationService.MineResp {
        return IAuthenticationService.MineResp.Administrator(Unit)
    }
}