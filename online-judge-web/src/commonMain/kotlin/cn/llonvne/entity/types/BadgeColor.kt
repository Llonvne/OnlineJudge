package cn.llonvne.entity.types

import kotlinx.serialization.Serializable

@Serializable
enum class BadgeColor {
    Green,
    Red,
    Blue
}

/**
 * 为 [cn.llonvne.compoent.badgeGroup 提供支持]
 */
interface BadgeColorGetter {
    val color: BadgeColor
}