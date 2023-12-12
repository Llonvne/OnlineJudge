package cn.llonvne.gojudge.api.task.gpp

import cn.llonvne.gojudge.api.Task
import cn.llonvne.gojudge.api.spec.*
import cn.llonvne.gojudge.api.task.Output
import cn.llonvne.gojudge.exposed.RuntimeService
import cn.llonvne.gojudge.exposed.run
import cn.llonvne.gojudge.services.runtime.request
import cn.llonvne.gojudge.services.runtime.useUsrBinEnv
import com.benasher44.uuid.uuid4

class GppCompileTask : Task<GppInput, Output> {
    private val String.cpp: String
        get() = "$this.cpp"

    override suspend fun run(input: GppInput, service: RuntimeService): Output {
        val sourceCodeFilename = uuid4().toString().cpp
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
            service.run(compileRequest).getOrNull(0) ?: return Output.Failure.CompileResultIsNull(
                compileRequest
            )

        if (compileResult.status != Status.Accepted) {
            return Output.Failure.CompileError(compileRequest, compileResult)
        }

        val fileId =
            compileResult.fileIds?.get(outputCodeFilename) ?: return Output.Failure.TargetFileNotExist(
                compileRequest,
                compileResult
            )

        val runFilename = uuid4().toString()

        val runRequest = request {
            cmd {
                args = listOf(runFilename)
                env = useUsrBinEnv
                files = useStdOutErrForFiles(input.stdin)
                copyIn = useFileIdCopyIn(fileId = fileId, newName = runFilename)
            }
        }

        val runResult = service.run(runRequest).getOrNull(0) ?: return Output.Failure.RunResultIsNull(
            compileRequest,
            compileResult,
            runRequest
        )

        return Output.Success(
            compileRequest,
            compileResult,
            runRequest,
            runResult
        )
    }
}