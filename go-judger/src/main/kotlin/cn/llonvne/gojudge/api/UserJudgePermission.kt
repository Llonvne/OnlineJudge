package cn.llonvne.gojudge.api

import io.ktor.server.application.*

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

//val RACE_LIMIT_JUDGE_NAME = RateLimitName("judge")

