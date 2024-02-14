package cn.llonvne.gojudge.api.task.kotlin

import arrow.core.Option
import arrow.core.some
import cn.llonvne.gojudge.api.Task
import cn.llonvne.gojudge.api.spec.runtime.*
import cn.llonvne.gojudge.api.task.AbstractTask
import cn.llonvne.gojudge.api.task.CodeInput
import cn.llonvne.gojudge.api.task.Output
import cn.llonvne.gojudge.internal.cmd

fun kotlinCompileTask(): Task<CodeInput, Output> = KotlinCompileTask()

internal class KotlinCompileTask : AbstractTask<CodeInput>() {
    override val sourceCodeExtension: Option<String>
        get() = "kt".some()
    override val compiledFileExtension: Option<String>
        get() = "class".some()

    override fun transformSourceOrCompiledFilename(filenames: Filenames): Filenames {
        return filenames.copy(source = Filename("Main", "kt".some()), compiled = Filename("MainKt", "class".some()))
    }

    override fun getCompileCmd(input: CodeInput, filenames: Filenames): Cmd {
        return cmd {
            args = useKotlincArgs(filenames)
            env = useUsrBinEnv
            files = useStdOutErrForFiles()
            copyIn = useMemoryCodeCopyIn(filenames.source.asString(), input.code)
            copyOut = useStdOutErrForCopyOut()
            copyOutCached =
                listOf(filenames.source.asString(), filenames.compiled.asString())
        }
    }

    override fun transformCompileStatus(compileStatus: Status, compileResult: Result): Status {
        return if (compileResult.exitStatus == 0) {
            Status.Accepted
        } else {
            compileStatus
        }
    }

    override fun transformRunFilename(filename: Filename): Filename {
        return Filename("MainKt", "class".some())
    }

    override fun getRunCmd(input: CodeInput, compileResult: Result, runFilename: Filename, runFileId: String) = cmd {
        args = listOf("kotlin", runFilename.name)
        env = useUsrBinEnv
        files = useStdOutErrForFiles(input.stdin)
        copyIn = useFileIdCopyIn(fileId = runFileId, newName = runFilename.asString())
    }
}

internal fun useKotlincArgs(filenames: AbstractTask.Filenames) =
    listOf("kotlinc", filenames.source.asString())