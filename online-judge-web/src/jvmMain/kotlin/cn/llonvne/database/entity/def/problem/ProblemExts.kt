package cn.llonvne.database.entity.def.problem

import cn.llonvne.entity.problem.context.Problem
import cn.llonvne.kvision.service.IProblemService.CreateProblemReq
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json

fun Problem.Companion.fromCreateReq(
    createProblemReq: CreateProblemReq,
    ownerId: Int,
) = Problem(
    authorId = createProblemReq.authorId,
    problemName = createProblemReq.problemName,
    memoryLimit = createProblemReq.memoryLimit,
    timeLimit = createProblemReq.timeLimit,
    problemDescription = createProblemReq.problemDescription,
    visibility = createProblemReq.visibility,
    type = createProblemReq.type,
    contextJson = json.encodeToString(createProblemReq.problemContext),
    ownerId = ownerId,
)
