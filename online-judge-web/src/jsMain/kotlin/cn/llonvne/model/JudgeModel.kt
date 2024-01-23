package cn.llonvne.model

import cn.llonvne.kvision.service.IJudgeService
import io.kvision.remote.getService

object JudgeModel {
    private val judgeService = getService<IJudgeService>()
}