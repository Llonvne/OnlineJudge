package cn.llonvne.gojudge.task.gpp.task

import cn.llonvne.gojudge.api.GoJudgeService
import cn.llonvne.gojudge.api.Status
import cn.llonvne.gojudge.api.ext.*
import cn.llonvne.gojudge.api.ext.request
import cn.llonvne.gojudge.api.ext.useGpp
import cn.llonvne.gojudge.api.ext.useStdOutErrForFiles
import cn.llonvne.gojudge.api.ext.useUsrBinEnv
import cn.llonvne.gojudge.api.goJudgeClient
import cn.llonvne.gojudge.task.Task
import cn.llonvne.gojudge.task.gpp.api.GppInput
import cn.llonvne.gojudge.task.gpp.api.GppOutput
import java.util.*

class GppCompileTask : Task<GppInput, GppOutput> {
    private val String.cpp: String
        get() = "$this.cc"

    override suspend fun run(input: GppInput, service: GoJudgeService): GppOutput {
        val sourceCodeFilename = UUID.randomUUID().toString().cpp
        val outputCodeFilename = UUID.randomUUID().toString()

        val compileRequest = request {
            cmd {
                args = useGpp(sourceCodeFilename, outputCodeFilename)
                env = useUsrBinEnv
                files = useStdOutErrForFiles()
                copyIn = useMemoryCodeCopyIn(sourceCodeFilename, input.code)
                copyOut = useStdOutErrForCopyOut()
                copyOutCached = listOf(sourceCodeFilename, outputCodeFilename)
            }
        }

        val compileResult =
            goJudgeClient.run(compileRequest).getOrNull(0) ?: return GppOutput.Failure.CompileResultIsNull(
                compileRequest
            )

        if (compileResult.status != Status.Accepted) {
            return GppOutput.Failure.CompileError(compileRequest, compileResult)
        }

        val fileId =
            compileResult.fileIds?.get(outputCodeFilename) ?: return GppOutput.Failure.TargetFileNotExist(
                compileRequest,
                compileResult
            )

        val runFilename = UUID.randomUUID().toString()

        val runRequest = request {
            cmd {
                args = listOf(runFilename)
                env = useUsrBinEnv
                files = useStdOutErrForFiles()
                copyIn = useFileIdCopyIn(fileId = fileId, newName = runFilename)
            }
        }

        val runResult = goJudgeClient.run(runRequest).getOrNull(0) ?: return GppOutput.Failure.RunResultIsNull(
            compileRequest,
            compileResult,
            runRequest
        )

        return GppOutput.Success(
            compileRequest,
            compileResult,
            runRequest,
            runResult
        )
    }
}