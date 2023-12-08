@file:Suppress("UNUSED")

package cn.llonvne.gojudge.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface GoJudgeFile {
    @Serializable
    data class LocalFile(
        val src: String // absolute path for the file
    ) : GoJudgeFile

    @Serializable
    data class MemoryFile(
        val content: String // file contents, can be String or ByteArray (Buffer in TypeScript)
    ) : GoJudgeFile

    @Serializable
    data class PreparedFile(
        val fileId: String // fileId defines file uploaded by /file
    ) : GoJudgeFile

    @Serializable
    data class Collector(
        val name: String, // file name in copyOut
        val max: Int,  // maximum bytes to collect from pipe
        val pipe: Boolean? = null // collect over pipe or not (default false)
    ) : GoJudgeFile
}

@Serializable
data class Symlink(
    val symlink: String // symlink destination (v1.6.0+)
)

@Serializable
data class Cmd(
    var args: List<String>, // command line argument
    var env: List<String>? = null, // environment

    // specifies file input / pipe collector for program file descriptors
    var files: List<GoJudgeFile>? = null, // Any can be LocalFile, MemoryFile, PreparedFile, Collector
    var tty: Boolean? = null, // enables tty on the input and output pipes (should have just one input & one output)
    // Notice: must have TERM environment variables (e.g. TERM=xterm)

    // limitations
    var cpuLimit: Long? = null,     // ns
    var realCpuLimit: Long? = null, // deprecated: use clock limit instead (still working)
    var clockLimit: Long? = null,   // ns
    var memoryLimit: Long? = null,  // byte
    var stackLimit: Long? = null,   // byte (N/A on windows, macOS cannot set over 32M)
    var procLimit: Int? = null,
    var cpuRateLimit: Int? = null, // limit cpu usage (1000 equals 1 cpu)
    var cpuSetLimit: String? = null, // Linux only: set the cpuSet for cgroup
    var strictMemoryLimit: Boolean? = null, // deprecated: use dataSegmentLimit instead (still working)
    var dataSegmentLimit: Boolean? = null, // Linux only: use (+ rlimit_data limit) enable by default if cgroup not enabled
    var addressSpaceLimit: Boolean? = null, // Linux only: use (+ rlimit_address_space limit)

    // copy the correspond file to the container dst path
    var copyIn: Map<String, GoJudgeFile>? = null, // Any can be LocalFile, MemoryFile, PreparedFile, Symlink

    // copy out specifies files need to be copied out from the container after execution
    // append '?' after file name will make the file optional and do not cause FileError when missing
    var copyOut: List<String>? = null,
    // similar to copyOut but stores file in go judge and returns fileId, later download through /file/:fileId
    var copyOutCached: List<String>? = null,
    // specifies the directory to dump container /w content
    var copyOutDir: String? = null,
    // specifies the max file size to copy out
    var copyOutMax: Int? = null // byte
)

@Serializable
enum class Status {
    Accepted, // normal

    @SerialName("Memory Limit Exceeded")
    MemoryLimitExceeded, // mle

    @SerialName("Time Limit Exceeded")
    TimeLimitExceeded, // tle

    @SerialName("Output Limit Exceeded")
    OutputLimitExceeded, // ole

    @SerialName("File Error")
    FileError, // fe

    @SerialName("Nonzero Exit Status")
    NonzeroExitStatus,
    Signalled,

    @SerialName("Internal Error")
    InternalError // system error
}

@Serializable
data class PipeIndex(
    val index: Int, // the index of cmd
    val fd: Int    // the fd number of cmd
)

@Serializable
data class PipeMap(
    val `in`: PipeIndex,  // input end of the pipe
    val out: PipeIndex, // output end of the pipe
    // enable pipe proxy from in to out,
    // content from in will be discarded if out closes
    val proxy: Boolean? = null,
    val name: String? = null,   // copy out proxy content if proxy enabled
    // limit the copy out content size,
    // proxy will still functioning after max
    val max: Int? = null
)

@Serializable
enum class FileErrorType {
    CopyInOpenFile,
    CopyInCreateFile,
    CopyInCopyContent,
    CopyOutOpen,
    CopyOutNotRegularFile,
    CopyOutSizeExceeded,
    CopyOutCreateFile,
    CopyOutCopyContent,
    CollectSizeExceeded
}

@Serializable
data class FileError(
    val name: String, // error file name
    val type: FileErrorType, // type
    val message: String? = null // detailed message
)

@Serializable
sealed interface RequestType {
    @Serializable
    data class Request(
        val requestId: String? = null, // for WebSocket requests
        val cmd: List<Cmd>,
        val pipeMapping: List<PipeMap>? = null
    ) : RequestType

    @Serializable
    data class CancelRequest(
        val cancelRequestId: String
    ) : RequestType
}

// WebSocket request
typealias WSRequest = RequestType // Any can be Request or CancelRequest

@Serializable
data class Result(
    val status: Status,
    val error: String? = null, // potential system error message
    val exitStatus: Int,
    val time: Long,   // ns (cgroup recorded time)
    val memory: Long, // byte
    val runTime: Long, // ns (wall clock time)
    // copyFile name -> content
    val files: Map<String, String?>? = null,
    // copyFileCached name -> fileId
    val fileIds: Map<String, String>? = null,
    // fileError contains detailed file errors
    val fileError: List<FileError>? = null
)

@Serializable
// WebSocket results
data class WSResult(
    val requestId: String,
    val results: List<Result>,
    val error: String? = null
)