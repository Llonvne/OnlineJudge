package cn.llonvne.gojudge.ktor

import cn.llonvne.gojudge.api.JUDGE_TOKEN_REFILL_DURATION
import cn.llonvne.gojudge.api.TOTAL_TOKEN_IN_DURATION
import cn.llonvne.gojudge.api.UserJudgePermission
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*

val RACE_LIMIT_JUDGE_NAME = RateLimitName("judge")

fun Application.installJudgeRateLimit() {
    install(RateLimit) {
        register(RACE_LIMIT_JUDGE_NAME) {
            // 每个用户一分钟 1000 个令牌
            rateLimiter(limit = TOTAL_TOKEN_IN_DURATION, refillPeriod = JUDGE_TOKEN_REFILL_DURATION)

            // 获得用户代码
            requestKey {
                it.userJudgePermission
            }

            // 判断用户身份
            requestWeight { call, key ->
                if (key is UserJudgePermission) {
                    key.costTokenPer
                } else {
                    TOTAL_TOKEN_IN_DURATION
                }
            }
        }
    }
}