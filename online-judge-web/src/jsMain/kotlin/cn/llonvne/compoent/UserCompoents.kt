package cn.llonvne.compoent

import cn.llonvne.constants.Authentication
import io.kvision.form.FormPanel
import io.kvision.form.text.Password
import io.kvision.form.text.Text
import kotlin.reflect.KProperty1

inline fun <reified K : Any> FormPanel<K>.addUsername(key: KProperty1<K, String?>) =
    add(key,
        Text(label = "username") { placeholder = "输入你的用户名" },
        required = true,
        requiredMessage = "必须输入你的用户名",
        validator = { text: Text -> Authentication.User.Name.check(text.getValue()).isOk() },
        validatorMessage = { text -> Authentication.User.Name.reason(text.getValue()) })

inline fun <reified K : Any> FormPanel<K>.addPassword(key: KProperty1<K, String?>) = add(
    key,
    Password(label = "password"),
    required = true,
    requiredMessage = "密码不得为空",
    validator = { password -> Authentication.User.Password.check(password.value).isOk() },
    validatorMessage = { password -> Authentication.User.Password.reason(password.value) }
)