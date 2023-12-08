package cn.llonvne.gojudge.api

import com.benasher44.uuid.uuid4

class GppCompileTask(val goJudgeService: GoJudgeService) : Task<GppInput, GppOutput> {
    private val String.cpp: String
        get() = "$this.cc"

    override suspend fun run(input: GppInput, service: GoJudgeService): GppOutput {
        val sourceCodeFilename = uuid4().toString()
        val outputCodeFilename = uuid4().toString()

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
            goJudgeService.run(compileRequest).getOrNull(0) ?: return GppOutput.Failure.CompileResultIsNull(
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

        val runFilename = uuid4().toString()

        val runRequest = request {
            cmd {
                args = listOf(runFilename)
                env = useUsrBinEnv
                files = useStdOutErrForFiles()
                copyIn = useFileIdCopyIn(fileId = fileId, newName = runFilename)
            }
        }

        val runResult = goJudgeService.run(runRequest).getOrNull(0) ?: return GppOutput.Failure.RunResultIsNull(
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