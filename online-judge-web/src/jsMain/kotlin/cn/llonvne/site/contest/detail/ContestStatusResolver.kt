package cn.llonvne.site.contest.detail

import cn.llonvne.compoent.AlertType
import cn.llonvne.entity.contest.Contest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.js.Date

class ContestStatusResolver(private val startAt: LocalDateTime,private val endAt: LocalDateTime) {
    fun status(): Contest.ContestStatus {
        val instant = Instant.fromEpochMilliseconds(Date.now().toLong())
        return if (instant < startAt.toInstant(TimeZone.currentSystemDefault())) {
            Contest.ContestStatus.NotBegin
        } else if (instant < endAt.toInstant(TimeZone.currentSystemDefault())) {
            Contest.ContestStatus.Running
        } else {
            Contest.ContestStatus.Ended
        }
    }

    fun statusColor(): AlertType {
        return when (status()) {
            Contest.ContestStatus.NotBegin -> AlertType.Info
            Contest.ContestStatus.Running -> AlertType.Success
            Contest.ContestStatus.Ended -> AlertType.Danger
        }
    }
}