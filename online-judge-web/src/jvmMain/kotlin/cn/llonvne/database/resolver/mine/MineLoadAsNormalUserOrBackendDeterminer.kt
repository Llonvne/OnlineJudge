package cn.llonvne.database.resolver.mine

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.role.Backend
import cn.llonvne.getLogger
import cn.llonvne.kvision.service.IAuthenticationService
import cn.llonvne.security.check
import org.springframework.stereotype.Service

@Service
class MineLoadAsNormalUserOrBackendDeterminer(
    private val backendMineResolver: BackendMineResolver,
    private val normalMineResolver: NormalMineResolver
) {
    private val logger = getLogger()
    suspend fun resolve(user: AuthenticationUser): IAuthenticationService.MineResp {

        logger.info("用户${user.id} 读取个人界面，检查 Backend 权限")

        return if (user.check(Backend.BackendImpl)) {

            logger.info("用户${user.id} 拥有 Backend 权限，读取后台界面")

            backendMineResolver.resolve(user)
        } else {

            logger.info("用户${user.id} 未拥有 Backend 权限，读取个人界面")

            normalMineResolver.resolve(user)
        }
    }
}