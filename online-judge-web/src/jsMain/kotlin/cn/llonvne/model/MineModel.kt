package cn.llonvne.model

import cn.llonvne.entity.ModifyUserForm
import cn.llonvne.kvision.service.IMineService
import io.kvision.remote.getService

object MineModel {
    private val mineService = getService<IMineService>()

    suspend fun dashboard() =
        mineService.dashboard(
            AuthenticationModel.userToken.value,
        )

    suspend fun users() = mineService.users()

    suspend fun deleteUser(id: Int) = mineService.deleteUser(AuthenticationModel.userToken.value, id)

    suspend fun modifyUser(result: ModifyUserForm?): Boolean {
        if (result == null) {
            return false
        }

        return mineService.modifyUser(AuthenticationModel.userToken.value, result)
    }
}
