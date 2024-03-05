package cn.llonvne.site.contest.detail

import cn.llonvne.compoent.AlertType
import cn.llonvne.entity.contest.Contest
import cn.llonvne.entity.contest.ContestId
import cn.llonvne.kvision.service.IContestService
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.js.Date

class ContestStatusResolver(private val loadOk: IContestService.LoadContestResp.LoadOk) {
    fun status(): Contest.ContestStatus {
        val instant = Instant.fromEpochMilliseconds(Date.now().toLong())
        return if (instant < loadOk.contest.startAt.toInstant(TimeZone.currentSystemDefault())) {
            Contest.ContestStatus.NotBegin
        } else if (instant < loadOk.contest.endAt.toInstant(TimeZone.currentSystemDefault())) {
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