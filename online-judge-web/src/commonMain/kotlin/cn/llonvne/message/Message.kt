package cn.llonvne.message

import kotlinx.serialization.Serializable

@Serializable
sealed interface Message {
    @Serializable
    val level: MessageLevel

    /**
     * 指示该信息将由 Toast 发出
     * @see [cn.llonvne.message.Messager.toastByLevel]
     */
    @Serializable
    data class ToastMessage(override val level: MessageLevel, val message: String) :
        Message
}