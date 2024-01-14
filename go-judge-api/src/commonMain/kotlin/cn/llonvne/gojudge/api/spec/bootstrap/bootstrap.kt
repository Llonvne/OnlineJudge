@file:Suppress("unused")

package cn.llonvne.gojudge.api.spec.bootstrap

import arrow.optics.optics
import cn.llonvne.gojudge.api.spec.LATEST
import cn.llonvne.gojudge.api.spec.LOCALHOST
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
sealed interface GoJudgeVersion {
    val tag: String

    @Serializable
    data object Latest : GoJudgeVersion {
        override val tag: String = LATEST
    }

    @Serializable
    data class Customized(override val tag: String) : GoJudgeVersion
}

@Serializable
data class GoJudgePortMapping(
    val outer: Int = 5050, val inner: Int = 5050
) {
    init {
        require(isValidPort(outer)) {
            "outer:$outer is not a valid port in 1..65535"
        }
        require(isValidPort(inner)) {
            "inner:$inner is not a valid port in 1..65535"
        }
    }

    val asDockerPortMappingString get() = "$inner:$outer"
}

@Serializable
sealed interface GoJudgePortMappings {
    val binds: List<GoJudgePortMapping>

    @Serializable
    data object Default : GoJudgePortMappings {
        override val binds: List<GoJudgePortMapping> = listOf(GoJudgePortMapping())
    }

    @Serializable
    data class Customized(override val binds: List<GoJudgePortMapping>) : GoJudgePortMappings
}

@Serializable
sealed interface ContainerName {
    val name: String

    @Serializable
    data class GeneratorWithPrefix(val prefix: String, val randomLatterLength: Int = 6) : ContainerName {
        override val name: String = "$prefix:${uuid4().toString().subSequence(0..randomLatterLength)}"
    }

    data class Customized(override val name: String) : ContainerName
}

@Serializable
@optics
/**
 * 表示一个网络地址
 * 会对 [port] 正确性检查，不会对 [url] 进行任何检查
 */
data class HttpAddr(val url: String, val port: Int) {
    init {
        require(isValidPort(port))
    }

    override fun toString(): String {
        return "$url:$port"
    }

    companion object
}

/**
 * 指示是否为默认值，该变量不会在序列化时被表示
 */
interface IsDefaultSetting {
    @Transient
    val isDefault: Boolean
}

@Serializable
@optics
/**
 * 表示 Go-Judge 启动时
 */
data class GoJudgeEnvSpec(
    val httpAddr: HttpAddr = DEFAULT_HTTP_ADDR,
    val enableGrpc: Boolean = DEFAULT_ENABLE_GRPC,
    val grpcAddr: HttpAddr = DEFAULT_GRPC_ADDR,
    val logLevel: GoJudgeLogLevel = GoJudgeLogLevel.INFO,
    val authToken: GoJudgeAuthTokenSetting = GoJudgeAuthTokenSetting.Disabled,
    val goDebugEndPoint: Boolean = DEFAULT_GO_DEBUG_ENDPOINT_ENABLE,
    val prometheusMetrics: Boolean = DEFAULT_PROMETHEUS_METRICS_ENDPOINT_ENABLE,
    val goDebugAndPrometheusMetricsAddr: HttpAddr = DEFAULT_MONITOR_ADDR,
    val concurrencyNumber: ConcurrencyNumberSetting = ConcurrencyNumberSetting.EqualCpuCore,
    val fileStore: FileStoreSetting = FileStoreSetting.Memory,
    // 默认的CGroup前缀是 gojudge ，可以用 -cgroup-prefix flag指定。
    val cGroupPrefix: CGroupPrefixSetting = CGroupPrefixSetting.Default,
    // 限制 src copyIn 路径用逗号分割（必须是绝对路径）（示例： /bin,/usr ）
    val srcPrefix: List<String> = listOf(),
    val timeLimitCheckerInterval: GoJudgeTimeInterval = GoJudgeTimeInterval.Default,
    val outputLimit: OutputLimitSetting = OutputLimitSetting.Default,
    // 指定用于检查超出内存限制的额外内存限制（默认为 16KiB）
    val extraMemoryLimit: ExtraMemoryLimitSetting = ExtraMemoryLimitSetting.Default,
    val copyOutLimit: CopyOutLimitSetting = CopyOutLimitSetting.Default,
    val openFileLimit: OpenFileLimitSetting = OpenFileLimitSetting.Default,
    val linuxOnlySpec: LinuxOnlySpec = LinuxOnlySpec.default(),
    val preFork: PreForkSetting = PreForkSetting.Default,
    val fileTimeout: FileTimeoutSetting = FileTimeoutSetting.Disabled
) {
    companion object {
        val DEFAULT_HTTP_ADDR = HttpAddr(LOCALHOST, 5050)

        val DEFAULT_GRPC_ADDR = HttpAddr(LOCALHOST, 5051)

        const val DEFAULT_ENABLE_GRPC = false

        const val DEFAULT_GO_DEBUG_ENDPOINT_ENABLE = false

        const val DEFAULT_PROMETHEUS_METRICS_ENDPOINT_ENABLE = false

        val DEFAULT_MONITOR_ADDR = HttpAddr(LOCALHOST, 5052)
    }

    @Serializable
    @optics
    sealed interface GoJudgeLogLevel : IsDefaultSetting {
        companion object {}

        override val isDefault get() = this is INFO

        @Serializable
        data object INFO : GoJudgeLogLevel

        @Serializable
        data object SILENT : GoJudgeLogLevel

        @Serializable
        data object RELEASE : GoJudgeLogLevel
    }

    @Serializable
    @optics
    sealed interface GoJudgeAuthTokenSetting : IsDefaultSetting {
        companion object {}

        override val isDefault: Boolean get() = this is Disabled

        @Serializable
        data object Disabled : GoJudgeAuthTokenSetting

        @Serializable
        @optics
        data class Enable(val token: String) : GoJudgeAuthTokenSetting {
            companion object
        }
    }

    @Serializable
    @optics
    sealed interface ConcurrencyNumberSetting : IsDefaultSetting {
        override val isDefault: Boolean get() = this is EqualCpuCore

        @Serializable
        data object EqualCpuCore : ConcurrencyNumberSetting

        @Serializable
        @optics
        data class Customized(val number: Int) : ConcurrencyNumberSetting {
            init {
                require(number > 0)
            }

            companion object
        }

        companion object
    }

    @Serializable
    sealed interface FileStoreSetting : IsDefaultSetting {
        override val isDefault: Boolean get() = this is Memory

        @Serializable
        data object Memory : FileStoreSetting

        @Serializable
        data object Dir : FileStoreSetting
    }

    @Serializable
    @optics
    sealed interface CGroupPrefixSetting : IsDefaultSetting {

        val prefix: String

        override val isDefault: Boolean get() = this is Default

        @Serializable
        data object Default : CGroupPrefixSetting {
            override val prefix: String = "gojudge"
        }

        @Serializable
        @optics
        data class Customized(override val prefix: String) : CGroupPrefixSetting {
            companion object
        }

        companion object
    }

    @Serializable
    @optics
    sealed interface GoJudgeTimeInterval : IsDefaultSetting {
        val time: String
        override val isDefault: Boolean get() = this is Default

        @Serializable
        data object Default : GoJudgeTimeInterval {
            override val time: String = "100ms"
        }

        @Serializable
        @optics
        data class Ms(val ms: Int) : GoJudgeTimeInterval {
            override val time: String = "${ms}ms"

            companion object
        }

        @Serializable
        @optics
        data class Second(val second: Int) : GoJudgeTimeInterval {
            override val time: String = "${second}s"

            companion object
        }

        companion object
    }

    @Serializable
    @optics
    sealed interface OutputLimitSetting : IsDefaultSetting {

        val byte: Long

        override val isDefault: Boolean
            get() = this is Default

        @Serializable
        data object Default : OutputLimitSetting {
            override val byte: Long
                get() = 256L.Mib
        }

        @Serializable
        @optics
        data class Customize(override val byte: Long) : OutputLimitSetting {
            companion object
        }

        companion object
    }

    @Serializable
    @optics
    sealed interface ExtraMemoryLimitSetting : IsDefaultSetting {
        val byte: Long

        override val isDefault: Boolean
            get() = this is Default

        @Serializable
        data object Default : ExtraMemoryLimitSetting {
            override val byte: Long = 16L.Kib
        }

        @Serializable
        @optics
        data class Customized(override val byte: Long) : ExtraMemoryLimitSetting {
            companion object
        }

        companion object
    }

    @Serializable
    @optics
    sealed interface CopyOutLimitSetting : IsDefaultSetting {
        val byte: Long
        override val isDefault: Boolean
            get() = this is Default

        @Serializable
        data object Default : CopyOutLimitSetting {
            override val byte: Long = 64L.Mib
        }

        @Serializable
        @optics
        data class Customized(override val byte: Long) : CopyOutLimitSetting {
            companion object
        }

        companion object
    }

    @Serializable
    @optics
    sealed interface OpenFileLimitSetting : IsDefaultSetting {
        val limit: Long
        override val isDefault: Boolean
            get() = this is Default

        @Serializable
        data object Default : OpenFileLimitSetting {
            override val limit: Long = 256
        }

        @Serializable
        @optics
        data class Customized(override val limit: Long) : OpenFileLimitSetting {
            companion object
        }

        companion object
    }

    @Serializable
    @optics
    sealed interface LinuxOnlySpec : IsDefaultSetting {
        @Serializable
        data object NotLinuxPlatform : LinuxOnlySpec {
            override val isDefault: Boolean
                get() = true
        }

        companion object {
            fun default() = NotLinuxPlatform
        }

        @Serializable
        @optics
        data class LinuxPlatformSpec(
            // specifies cpuset.cpus cgroup for each container (Linux only)
            val cpuSets: CpuSetting = CpuSetting.Default,
            val containerCredStart: ContainerCredStartSetting = ContainerCredStartSetting.Default,
            val enableCpuRate: CpuRateSetting = CpuRateSetting.Disable,
            // specifies seecomp filter setting to load when running program (need build tag seccomp) (Linux only)
            val seccompConf: SeccompConfSetting = SeccompConfSetting.Disable,
            // 指定使用默认挂载时 /tmp 的 /w tmpfs 参数（仅限 Linux）
            val tmpFsParam: TmsFsParamSetting = TmsFsParamSetting.Default,
            val mountConf: MountConfSetting = MountConfSetting.Default
        ) : LinuxOnlySpec {
            override val isDefault: Boolean
                get() = false

            companion object {}

            @Serializable
            @optics
            sealed interface CpuSetting : IsDefaultSetting {

                override val isDefault: Boolean
                    get() = this is Default

                @Serializable
                data object Default : CpuSetting

                @Serializable
                @optics
                data class Customized(val settings: String) : CpuSetting {
                    companion object
                }

                companion object
            }

            @Serializable
            @optics
            sealed interface ContainerCredStartSetting : IsDefaultSetting {
                val start: Int

                override val isDefault: Boolean
                    get() = this is Default

                @Serializable
                data object Default : ContainerCredStartSetting {
                    override val start: Int
                        get() = 10000
                }

                @Serializable
                @optics
                data class Customized(override val start: Int) : ContainerCredStartSetting {
                    companion object
                }

                companion object
            }

            @Serializable
            @optics
            sealed interface CpuRateSetting : IsDefaultSetting {
                override val isDefault: Boolean
                    get() = this is Disable

                @Serializable
                data object Disable : CpuRateSetting

                @Serializable
                data object Enable : CpuRateSetting

                companion object
            }

            @Serializable
            @optics
            sealed interface SeccompConfSetting : IsDefaultSetting {
                override val isDefault: Boolean
                    get() = this is Disable

                @Serializable
                data object Disable : SeccompConfSetting

                @Serializable
                @optics
                data class Customized(val settings: String) : SeccompConfSetting {
                    companion object
                }

                companion object
            }

            @Serializable
            @optics
            sealed interface TmsFsParamSetting : IsDefaultSetting {
                override val isDefault: Boolean
                    get() = this is Default

                @Serializable
                data object Default : TmsFsParamSetting

                @Serializable
                @optics
                data class Customized(val command: String) : TmsFsParamSetting {
                    companion object
                }

                companion object
            }

            @Serializable
            @optics
            sealed interface MountConfSetting : IsDefaultSetting {
                override val isDefault: Boolean
                    get() = this is Default

                @Serializable
                data object Default : MountConfSetting

                @Serializable
                @optics
                data class Customized(val settings: String) : MountConfSetting {
                    companion object
                }

                companion object
            }
        }
    }

    @Suppress("unused")
    @Serializable
    @optics
    sealed interface PreForkSetting : IsDefaultSetting {
        override val isDefault: Boolean
            get() = this is Default

        @Serializable
        data object Default : PreForkSetting

        @Serializable
        @optics
        data class Customized(val instance: Int) : PreForkSetting {
            companion object
        }

        companion object
    }

    @Serializable
    @optics
    sealed interface FileTimeoutSetting : IsDefaultSetting {
        override val isDefault: Boolean
            get() = this is Disabled

        @Serializable
        data object Disabled : FileTimeoutSetting

        @Serializable
        @optics
        data class Timeout(val minutes: Int) : FileTimeoutSetting {
            val seconds = (minutes * 60).toString() + "s"

            companion object
        }

        companion object
    }
}