package cn.llonvne.entity.types.badge

import kotlinx.serialization.Serializable

@Serializable
enum class BadgeColor {
    Green,
    Red,
    Blue,
    Grey,
    Golden,
    White,
    Dark
}

val BadgeColor.cssClass: String
    get() = when (this) {
        BadgeColor.Green -> "bg-success"
        BadgeColor.Red -> "bg-danger"
        BadgeColor.Blue -> "bg-primary"
        BadgeColor.Grey -> "text-bg-secondary"
        BadgeColor.Golden -> "text-bg-warning"
        BadgeColor.White -> "text-bg-light"
        BadgeColor.Dark -> "dark"
    }
