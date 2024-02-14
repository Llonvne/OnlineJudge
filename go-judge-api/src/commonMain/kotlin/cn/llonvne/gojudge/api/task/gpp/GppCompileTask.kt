package cn.llonvne.gojudge.api.task.gpp

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import cn.llonvne.gojudge.api.Task
import cn.llonvne.gojudge.api.spec.runtime.*
import cn.llonvne.gojudge.api.task.AbstractTask
import cn.llonvne.gojudge.api.task.CodeInput
import cn.llonvne.gojudge.api.task.Output
import cn.llonvne.gojudge.internal.cmd

fun gppCompileTask(cppVersion: CppVersion): Task<CodeInput, Output> = GppCompileTask(cppVersion)

private class GppCompileTask(private val cppVersion: CppVersion) : AbstractTask<CodeInput>() {
    override val sourceCodeExtension: Option<String>
        get() = "cpp".some()
    override val compiledFileExtension: Option<String>
        get() = None

    override fun getCompileCmd(input: CodeInput, filenames: Filenames): Cmd {
        return cmd {
            args = buildGppCompileCommand(filenames, cppVersion = cppVersion)
            env = useUsrBinEnv
            files = useStdOutErrForFiles()
            copyIn = useMemoryCodeCopyIn(filenames.source.asString(), input.code)
            copyOut = useStdOutErrForCopyOut()
            copyOutCached = listOf(filenames.source.asString(), filenames.compiled.asString())
        }
    }

    override fun getRunCmd(
        input: CodeInput,
        compileResult: Result,
        runFilename: Filename,
        runFileId: String
    ): Cmd {
        return cmd {
            args = listOf(runFilename.asString())
            env = useUsrBinEnv
            files = useStdOutErrForFiles(input.stdin)
            copyIn = useFileIdCopyIn(fileId = runFileId, newName = runFilename.asString())
        }
    }
}