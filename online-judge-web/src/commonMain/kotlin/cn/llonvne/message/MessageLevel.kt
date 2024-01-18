package cn.llonvne.message

import kotlinx.serialization.Serializable

@Serializable
enum class MessageLevel {
    Info,
    Warning,
    Danger,
    Success
}

