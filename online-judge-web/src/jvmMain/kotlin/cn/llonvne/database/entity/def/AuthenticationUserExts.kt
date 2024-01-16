package cn.llonvne.database.entity.def

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.exts.now
import kotlinx.datetime.LocalDateTime
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * 创建新用户使用的函数，时间将自动设置为现在的时间
 */
context(PasswordEncoder)
fun AuthenticationUser.Companion.createAtNow(
    username: String, rawPassword: String
) = AuthenticationUser(
    username = username,
    encryptedPassword = encode(rawPassword),
    createdAt = LocalDateTime.now(),
    updatedAt = LocalDateTime.now()
)