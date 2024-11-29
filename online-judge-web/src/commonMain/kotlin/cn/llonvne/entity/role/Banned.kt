package cn.llonvne.entity.role

import kotlinx.serialization.Serializable

@Serializable
sealed interface Banned : Role {
    companion object {
        val BannedImpl: Banned = BannedImplClass()
    }

    @Serializable
    class BannedImplClass : Banned {
        override fun check(provide: Role): Boolean = provide is Banned
    }
}
