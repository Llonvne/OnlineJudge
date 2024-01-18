import kotlinx.serialization.Serializable

object _Name {
    private const val NAME_MIN_LENGTH = 6
    private const val NAME_MAX_LENGTH = 40
    private const val VALIDATOR_MESSAGE =
        "用户名长度必须在${NAME_MIN_LENGTH}-${NAME_MAX_LENGTH}"

    fun reason(username: String?): String {
        return "$VALIDATOR_MESSAGE:${check(username)}"
    }

    fun check(username: String?): UsernameCheckResult {
        if (username == null) {
            return UsernameCheckResult.UsernameIsNull
        }

        if (username.length !in NAME_MIN_LENGTH..NAME_MAX_LENGTH) {
            return UsernameCheckResult.UsernameTooLongOrTooShort(username)
        }

        return UsernameCheckResult.Ok
    }

    @Serializable
    sealed interface UsernameCheckResult {

        fun isOk() = this is Ok

        @Serializable
        data object Ok : UsernameCheckResult

        @Serializable
        data object UsernameIsNull : UsernameCheckResult {
            override fun toString(): String {
                return "用户名为空"
            }
        }

        @Serializable
        data class UsernameTooLongOrTooShort(val username: String) : UsernameCheckResult {
            override fun toString(): String {
                return if (username.length < NAME_MIN_LENGTH) {
                    "用户名过短"
                } else {
                    "用户名过长"
                }
            }
        }
    }
}