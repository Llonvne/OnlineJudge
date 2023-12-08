package cn.llonvne.gojudge.api
import kotlin.time.Duration.Companion.seconds

enum class UserJudgePermission(val costTokenPer: Int) {
    Normal(100), Vip(10), Admin(0), Unregister(TOTAL_TOKEN_IN_DURATION)
}

const val TOTAL_TOKEN_IN_DURATION = 1000

const val KEY_IN_QUERY = "type"

val FALL_BACK_PERMISSION = UserJudgePermission.Unregister

val JUDGE_TOKEN_REFILL_DURATION = 60.seconds


