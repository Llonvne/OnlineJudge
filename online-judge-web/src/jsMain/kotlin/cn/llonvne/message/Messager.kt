package cn.llonvne.message

import cn.llonvne.message.Message.ToastMessage
import cn.llonvne.message.MessageLevel.*
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition

object Messager {
    fun send(message: Message) = when (message) {
        is ToastMessage -> {
            toastByLevel(message.level, message.message)
        }
    }

    /**
     * 将 [cn.llonvne.message.MessageLevel] 转换成 Toast 的登记并发出
     */
    private fun toastByLevel(level: MessageLevel, rawMessage: String) {
        when (level) {
            Info -> Toast.info(rawMessage, options = ToastOptions(ToastPosition.BOTTOMRIGHT))
            Warning -> Toast.warning(rawMessage, options = ToastOptions(ToastPosition.BOTTOMRIGHT))
            Danger -> Toast.danger(rawMessage, options = ToastOptions(ToastPosition.BOTTOMRIGHT))
            Success -> Toast.success(rawMessage, options = ToastOptions(ToastPosition.BOTTOMRIGHT))
        }
    }

    fun toastInfo(message: String) = send(
        ToastMessage(
            Info, message
        )
    )

    fun toastError(message: String) = send(
        ToastMessage(Danger, message)
    )
}