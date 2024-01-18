package cn.llonvne.message

import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition

object Messager {
    fun send(message: Message) = when (message) {
        is Message.ToastMessage -> {
            toastByLevel(message.level, message.message)
        }
    }

    /**
     * 将 [cn.llonvne.message.MessageLevel] 转换成 Toast 的登记并发出
     */
    private fun toastByLevel(level: MessageLevel, rawMessage: String) {
        when (level) {
            MessageLevel.Info -> Toast.info(rawMessage, options = ToastOptions(ToastPosition.BOTTOMRIGHT))
            MessageLevel.Warning -> Toast.warning(rawMessage, options = ToastOptions(ToastPosition.BOTTOMRIGHT))
            MessageLevel.Danger -> Toast.danger(rawMessage, options = ToastOptions(ToastPosition.BOTTOMRIGHT))
            MessageLevel.Success -> Toast.success(rawMessage, options = ToastOptions(ToastPosition.BOTTOMRIGHT))
        }
    }

    fun toastInfo(message: String) = send(
        Message.ToastMessage(
            MessageLevel.Info, message
        )
    )
}