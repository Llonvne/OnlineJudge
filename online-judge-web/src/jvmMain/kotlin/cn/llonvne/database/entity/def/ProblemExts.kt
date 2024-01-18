package cn.llonvne.database.entity.def

import cn.llonvne.entity.problem.Problem
import cn.llonvne.kvision.service.IProblemService

fun Problem.Companion.create(createProblemReq: IProblemService.CreateProblemReq) = Problem(
    authorId = createProblemReq.authorId,
    problemName = createProblemReq.problemName,
    memoryLimit = createProblemReq.memoryLimit,
    timeLimit = createProblemReq.timeLimit,
    problemDescription = createProblemReq.problemDescription
)