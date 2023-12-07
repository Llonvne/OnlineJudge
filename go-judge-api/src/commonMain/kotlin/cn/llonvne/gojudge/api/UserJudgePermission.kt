package cn.llonvne.gojudge.api

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.seconds

enum class UserJudgePermission(val costTokenPer: Int) {
    Normal(100), Vip(10), Admin(0), Unregister(TOTAL_TOKEN_IN_DURATION)
}

val ApplicationCall.userJudgePermission: UserJudgePermission
    get() {
        return try {
            UserJudgePermission.valueOf(
                this.request.queryParameters[KEY_IN_QUERY] ?: FALL_BACK_PERMISSION.name
            )
        } catch (e: IllegalArgumentException) {
            FALL_BACK_PERMISSION
        }
    }

const val TOTAL_TOKEN_IN_DURATION = 1000

const val KEY_IN_QUERY = "type"

val FALL_BACK_PERMISSION = UserJudgePermission.Unregister

val RACE_LIMIT_JUDGE_NAME = RateLimitName("judge")

val JUDGE_TOKEN_REFILL_DURATION = 60.seconds
