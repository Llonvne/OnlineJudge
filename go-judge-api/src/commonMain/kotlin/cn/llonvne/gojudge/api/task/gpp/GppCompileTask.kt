package cn.llonvne.gojudge.api.task.gpp

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import cn.llonvne.gojudge.api.spec.runtime.*
import cn.llonvne.gojudge.api.task.AbstractTask
import cn.llonvne.gojudge.api.task.CodeInput
import cn.llonvne.gojudge.services.runtime.cmd
import cn.llonvne.gojudge.services.runtime.useUsrBinEnv

class GppCompileTask : AbstractTask<CodeInput>() {
    override val sourceCodeExtension: Option<String>
        get() = "cpp".some()
    override val compiledFileExtension: Option<String>
        get() = None

    override fun getCompileCmd(input: CodeInput, filenames: Filenames): Cmd {
        return cmd {
            args = useGpp(filenames)
            env = useUsrBinEnv
            files = useStdOutErrForFiles()
            copyIn = useMemoryCodeCopyIn(filenames.source.asString(), input.code)
            copyOut = useStdOutErrForCopyOut()
            copyOutCached = listOf(filenames.source.asString(), filenames.compiled.asString())
        }
    }

    override fun getRunCmd(input: CodeInput, compileResult: cn.llonvne.gojudge.api.spec.runtime.Result, runFilename: Filename, runFileId: String): Cmd {
        return cmd {
            args = listOf(runFilename.asString())
            env = useUsrBinEnv
            files = useStdOutErrForFiles(input.stdin)
            copyIn = useFileIdCopyIn(fileId = runFileId, newName = runFilename.asString())
        }
    }
}