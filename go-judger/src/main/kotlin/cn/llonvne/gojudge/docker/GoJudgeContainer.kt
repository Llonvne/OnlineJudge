package cn.llonvne.gojudge.docker

import arrow.fx.coroutines.ExitCase
import arrow.fx.coroutines.resource
import arrow.fx.coroutines.resourceScope
import cn.llonvne.gojudge.api.GoJudgeEnvSpec
import cn.llonvne.gojudge.api.LATEST
import cn.llonvne.gojudge.api.portValidCheck
import com.github.dockerjava.api.model.HostConfig
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.util.*

private val GO_JUDGE_DOCKER_NAME = DockerImageName.parse("criyle/go-judge")

val isLinux by lazy {
    val dockerHost = System.getenv("DOCKER_HOST")

    if (dockerHost != null && dockerHost.startsWith("unix://")) {
        true
    } else {
        false
    }
}

@Suppress("unused")
sealed interface GoJudgeVersion {

    val tag: String

    data object Latest : GoJudgeVersion {
        override val tag: String = LATEST
    }

    data class Customized(override val tag: String) : GoJudgeVersion
}

data class GoJudgePortMapping(
    val outer: Int = 5050, val inner: Int = 5050
) {
    init {
        require(portValidCheck(outer)) {
            "outer:$outer is not a valid port in 1..65535"
        }
        require(portValidCheck(inner)) {
            "inner:$inner is not a valid port in 1..65535"
        }
    }

    val asDockerPortMappingString get() = "$inner:$outer"
}

sealed interface GoJudgePortMappings {
    val binds: List<GoJudgePortMapping>

    data object Default : GoJudgePortMappings {
        override val binds: List<GoJudgePortMapping> = listOf(GoJudgePortMapping())
    }

    @Suppress("unused")
    data class Customized(override val binds: List<GoJudgePortMapping>) : GoJudgePortMappings
}

sealed interface ContainerName {
    val name: String

    data class GeneratorWithPrefix(private val prefix: String, private val randomLatterLength: Int = 6) :
        ContainerName {
        override val name: String = "$prefix:${UUID.randomUUID().toString().subSequence(0..randomLatterLength)}"
    }

    @Suppress("unused")
    data class Customized(override val name: String) : ContainerName
}

suspend fun startGoJudgeContainer(
    version: GoJudgeVersion = GoJudgeVersion.Latest,
    portMappings: GoJudgePortMappings = GoJudgePortMappings.Default,
    isPrivilegedMode: Boolean = true,
    reuseContainer: Boolean = false,
    envsConfigure: GoJudgeEnvSpec.() -> Unit = {}
) {
    val logger = LoggerFactory.getLogger(GoJudgeContainer::class.java)

    // 初始化 DockerImage
    val container = GoJudgeContainer(GO_JUDGE_DOCKER_NAME.withTag(version.tag))


    // 处理暴露端口
    portMappings.binds.map { it.asDockerPortMappingString }.forEach { container.portBindings.add(it) }

    // 设置提权模式
    container.isPrivilegedMode = isPrivilegedMode

    // 是否需要复用
    container.withReuse(reuseContainer)

    val hostConfig = HostConfig()
    // 256.MB
    hostConfig.withShmSize(256 * 1024 * 1024L)
    container.withCreateContainerCmdModifier {
        it.withHostConfig(hostConfig)
    }

    // ENVS
    val spec = GoJudgeEnvSpec()
    spec.envsConfigure()


    resourceScope {
        resource(acquire = {
            container.start()
        }) { _, exit ->
            when (exit) {
                is ExitCase.Cancelled -> println("Go-Judge Container Exit Cancelled")
                ExitCase.Completed -> println("Go-Judge Container Exit Success")
                is ExitCase.Failure -> println("Go-Judge Container Exit Failure")
            }
        }


    }
}

fun shouldHappen(): Nothing =
    throw IllegalStateException("it shouldn't be info please report me on Github:Llonvne/OnlineJudge")

fun GoJudgeContainer.applySpec(spec: GoJudgeEnvSpec) {
    val logger = LoggerFactory.getLogger(GoJudgeContainer::class.java)

    if (spec.httpAddr != GoJudgeEnvSpec.DEFAULT_HTTP_ADDR) {
        withCommand("-http-addr ${spec.httpAddr}")

        logger.info("go-judge http endpoint on ${spec.httpAddr},port ${spec.httpAddr.port} is exposed")

        this.addExposedPort(spec.httpAddr.port)
    }

    if (spec.enableGrpc != GoJudgeEnvSpec.DEFAULT_ENABLE_GRPC) {
        withCommand("-enable-grpc ${spec.enableGrpc}")
        logger.info("go-judge grpc is active")
    }

    if (spec.grpcAddr != GoJudgeEnvSpec.DEFAULT_GRPC_ADDR) {

        if (spec.enableGrpc) {
            logger.error("you disable grpc service,and set a address,please check your setting")
        }
        withCommand("-grpc-addr ${spec.grpcAddr}")
        logger.info("go-judge grpc endpoint on ${spec.grpcAddr},port ${spec.grpcAddr.port} is exposed")

        this.addExposedPort(spec.grpcAddr.port)
    }

    if (!spec.logLevel.isDefault) {
        when (spec.logLevel) {
            GoJudgeEnvSpec.GoJudgeLogLevel.RELEASE -> withCommand("-release")
            GoJudgeEnvSpec.GoJudgeLogLevel.SILENT -> withCommand("-silent")
            GoJudgeEnvSpec.GoJudgeLogLevel.INFO -> shouldHappen()
        }
    }

    if (!spec.authToken.isDefault) {
        when (spec.authToken) {
            GoJudgeEnvSpec.GoJudgeAuthTokenSetting.Disabled -> shouldHappen()
            is GoJudgeEnvSpec.GoJudgeAuthTokenSetting.Enable -> {
                @Suppress("SMARTCAST_IMPOSSIBLE")
                withCommand("-auth-token ${spec.authToken.token}")
            }
        }
    }

    if (spec.goDebugEndPoint != GoJudgeEnvSpec.DEFAULT_GO_DEBUG_ENDPOINT_ENABLE) {
        withCommand("-enable-debug")
    }

    if (spec.prometheusMetrics != GoJudgeEnvSpec.DEFAULT_PROMETHEUS_METRICS_ENDPOINT_ENABLE) {
        withCommand("-enable-metrics")
    }

    if (spec.goDebugAndPrometheusMetricsAddr != GoJudgeEnvSpec.DEFAULT_MONITOR_ADDR) {
        withCommand("-monitor-addr ${spec.goDebugAndPrometheusMetricsAddr}")

        logger.info("go-judge monitor endpoint on ${spec.goDebugAndPrometheusMetricsAddr},port ${spec.goDebugAndPrometheusMetricsAddr.port} is exposed")

        if (!spec.prometheusMetrics && !spec.goDebugEndPoint) {
            logger.error("go-judge: go debug and prometheus metrics is all disabled,but set a addr for it,please check you setting")
        }

        this.addExposedPort(spec.goDebugAndPrometheusMetricsAddr.port)
    }

    @Suppress("SMARTCAST_IMPOSSIBLE")
    when (spec.concurrencyNumber) {
        is GoJudgeEnvSpec.ConcurrencyNumberSetting.Customized -> withCommand("-parallelism ${spec.concurrencyNumber.number}")
        GoJudgeEnvSpec.ConcurrencyNumberSetting.EqualCpuCore -> Unit
    }

    when (spec.fileStore) {
        GoJudgeEnvSpec.FileStoreSetting.Dir -> withCommand("-dir")
        GoJudgeEnvSpec.FileStoreSetting.Memory -> Unit
    }

    when (spec.cGroupPrefix) {
        is GoJudgeEnvSpec.CGroupPrefixSetting.Customized -> withCommand("-cgroup-prefix ${spec.cGroupPrefix.prefix}")
        GoJudgeEnvSpec.CGroupPrefixSetting.Default -> Unit
    }

    if (spec.srcPrefix.isNotEmpty()) {
        withCommand("-src-prefix ${spec.srcPrefix.joinToString(",")}")
    }

    when (spec.timeLimitCheckerInterval) {
        GoJudgeEnvSpec.GoJudgeTimeInterval.Default -> Unit
        is GoJudgeEnvSpec.GoJudgeTimeInterval.Ms -> withCommand("-time-limit-checker-interval ${spec.timeLimitCheckerInterval.time}")
        is GoJudgeEnvSpec.GoJudgeTimeInterval.Second -> withCommand("-time-limit-checker-interval ${spec.timeLimitCheckerInterval.time}")
    }

    when (spec.outputLimit) {
        is GoJudgeEnvSpec.OutputLimitSetting.Customize -> withCommand("-output-limit ${spec.outputLimit.byte} b")
        GoJudgeEnvSpec.OutputLimitSetting.Default -> Unit
    }

    when (spec.extraMemoryLimit) {
        is GoJudgeEnvSpec.ExtraMemoryLimitSetting.Customized -> withCommand("-extra-memory-limit ${spec.extraMemoryLimit.byte} b")
        GoJudgeEnvSpec.ExtraMemoryLimitSetting.Default -> Unit
    }

    when (spec.copyOutLimit) {
        is GoJudgeEnvSpec.CopyOutLimitSetting.Customized -> withCommand("-copy-out-limit ${spec.copyOutLimit.byte} b")
        GoJudgeEnvSpec.CopyOutLimitSetting.Default -> Unit
    }

    when (spec.openFileLimit) {
        GoJudgeEnvSpec.OpenFileLimitSetting.Default -> Unit
        is GoJudgeEnvSpec.OpenFileLimitSetting.Customized -> withCommand("-open-file-limit ${spec.openFileLimit.limit}")
    }
    @Suppress("SMARTCAST_IMPOSSIBLE")
    when (spec.linuxOnlySpec) {
        is GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec -> {
            if (!isLinux) {
                logger.error("You are not in a liunx platform but set some liunx setting,we will apply this setting,but may not valid")
            }

            val linuxSpec = spec.linuxOnlySpec as GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec

            when (linuxSpec.cpuSets) {
                is GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.CpuSetting.Customized -> withCommand("-cpuset ${(linuxSpec.cpuSets as GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.CpuSetting.Customized).settings}")
                GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.CpuSetting.Default -> Unit
            }

            when (linuxSpec.containerCredStart) {
                is GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.ContainerCredStartSetting.Customized -> withCommand("-container-cred-start ${linuxSpec.containerCredStart.start}")
                GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.ContainerCredStartSetting.Default -> Unit
            }

            when (linuxSpec.enableCpuRate) {
                GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.CpuRateSetting.Disable -> Unit
                is GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.CpuRateSetting.Enable -> withCommand("-enable-cpu-rate true")
            }

            when (linuxSpec.seccompConf) {
                is GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.SeccompConfSetting.Customized -> withCommand("-seccomp-conf ${(linuxSpec.seccompConf as GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.SeccompConfSetting.Customized).settings}")
                GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.SeccompConfSetting.Disable -> Unit
            }

            when (linuxSpec.tmpFsParam) {
                is GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.TmsFsParamSetting.Customized -> withCommand("-tmp-fs-param ${(linuxSpec.tmpFsParam as GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.TmsFsParamSetting.Customized).command}")
                GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.TmsFsParamSetting.Default -> Unit
            }

            when (linuxSpec.mountConf) {
                is GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.MountConfSetting.Customized -> withCommand("-mount-conf ${(linuxSpec.mountConf as GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.MountConfSetting.Customized).settings}")
                GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.MountConfSetting.Default -> Unit
            }
        }

        GoJudgeEnvSpec.LinuxOnlySpec.NotLinuxPlatform -> {
            Unit
        }
    }

    when (spec.preFork) {
        is GoJudgeEnvSpec.PreForkSetting.Customized -> withCommand("-pre-fork ${(spec.preFork as GoJudgeEnvSpec.PreForkSetting.Customized).instance}")
        GoJudgeEnvSpec.PreForkSetting.Default -> Unit
    }

    when (spec.fileTimeout) {
        GoJudgeEnvSpec.FileTimeoutSetting.Disabled -> Unit
        is GoJudgeEnvSpec.FileTimeoutSetting.Timeout -> withCommand("-file-timeout ${(spec.fileTimeout as GoJudgeEnvSpec.FileTimeoutSetting.Timeout).seconds}")
    }
}


class GoJudgeContainer internal constructor(
    imageName: DockerImageName,
) : GenericContainer<GoJudgeContainer>(imageName)