package cn.llonvne.constants

import kotlinx.serialization.Serializable

object _Password {
    private const val MIN_LENGTH = 6
    private const val MAX_LENGTH = 40
    private const val VALIDATOR_MESSAGE = "密码长度必须在${MIN_LENGTH}-${MAX_LENGTH}"

    fun check(password: String?): PasswordCheckResult {
        if (password == null) {
            return PasswordCheckResult.PasswordIsNull
        }

        if (password.length !in MIN_LENGTH..MAX_LENGTH) {
            return PasswordCheckResult.PasswordIsTooLongOrTooShort
        }

        return PasswordCheckResult.Ok
    }

    fun reason(password: String?): String = "$VALIDATOR_MESSAGE:${check(password)}"

    @Serializable
    sealed interface PasswordCheckResult {
        fun isOk() = this is Ok

        @Serializable
        data object Ok : PasswordCheckResult

        @Serializable
        data object PasswordIsTooLongOrTooShort : PasswordCheckResult {
            override fun toString(): String = "密码太长或者太短"
        }

        @Serializable
        data object PasswordIsNull : PasswordCheckResult {
            override fun toString(): String = "密码不能为空"
        }
    }
}
