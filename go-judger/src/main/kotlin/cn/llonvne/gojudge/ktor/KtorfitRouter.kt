package cn.llonvne.gojudge.ktor

import cn.llonvne.gojudge.exposed.KtorfitRouterService
import cn.llonvne.gojudge.api.ReflectionApi
import cn.llonvne.gojudge.exposed.Sample
import cn.llonvne.gojudge.docker.shouldHappen
import de.jensklingenberg.ktorfit.http.*
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions
import kotlin.reflect.full.superclasses


class KtorfitRouterConfig {
    var services: List<Any> = listOf<Any>()
}

typealias PIPE = PipelineInterceptor<Unit, ApplicationCall>

class SampleImpl : Sample {
    override suspend fun version(a: String): String {
        print("HelloWorld")
        return "Hello"
    }
}

@ReflectionApi
val KtorfitRouter = createApplicationPlugin("KtorfitRouter", ::KtorfitRouterConfig) {

    val log = KotlinLogging.logger { }

    application.environment.monitor.subscribe(ApplicationStarted) {
        pluginConfig.services.forEach { service ->

            // IMPL CLASS
            val implCls = service::class
            // IMPL METHOD
            val implMethods = implCls.functions.filterUserDefinedMethod()

            log.info { "process on ${implCls.simpleName}" }

            // ABSTRACT CLASS METHODS
            val abstractMethods = getSatisfiedMethodFromAbstract(getAnnotatedInterface(implCls), log)

            log.info { "find satisfied methods: ${abstractMethods.map { it.name }}" }

            // HTTP METHOD DESCR
            val httpMethodDescriptors = abstractMethods.map { method ->
                // filter Ktorfit Http Annotation
                val httpMethodUrls = method.annotations.filterKtorfitAnnotations().map { parseAnnotation(it) }
                HttpMethodDescriptor(method, httpMethodUrls)
            }

            // ROUTE
            val routing = it.pluginRegistry[AttributeKey("Routing")] as Routing

            // FOR EACH DESCR
            httpMethodDescriptors.forEach { descriptor ->

                // FOR EACH (PATH,METHOD)
                descriptor.httpMethodUrls.forEach { (path, method) ->

                    // FIND TARGET METHOD
                    val targetMethod = findMethod(implMethods, descriptor)
                    // PARSE METHOD PACK
                    val methodArgTypes = parseMethodArgTypes(targetMethod, service)
                    // GENERATE A ROUTE FOR PATH
                    val curRoute = routing.createRouteFromPath(path)


                    // APPLY METHOD TO ROUTE
                    curRoute.method(method) {
                        handle {
                            launch {
                                // CALL
                                val args = methodArgTypes.map {
                                    if (it.kotlinType?.isMarkedNullable == true) {
                                        call.receiveNullable<Any>(it)
                                    } else {
                                        call.receive(it)
                                    }
                                }

                                call.respond(targetMethod.callSuspend(service, *args.toTypedArray()) as Any)
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class ParameterType {
    Body, FormData, Header, HeadersMap, Tag, RequestBuilder
}

data class TypeInfoPack(val typeInfo: TypeInfo)

fun parseMethodArgTypes(targetMethod: KFunction<*>, service: Any): List<TypeInfo> {
    return targetMethod.parameters.map {
        TypeInfo(it.type.classifier as KClass<*>, it.type.platformType, it.type)
    }.filter {
        it.type != service::class
    }
}

private fun getAnnotatedInterface(implCls: KClass<out Any>): KClass<*> {
    val abstracts = implCls.superclasses.filter {
        it.annotations.any { annotation ->
            annotation.annotationClass.qualifiedName == KtorfitRouterService::class.qualifiedName
        }
    }.toList()

    val abstract = if (abstracts.size != 1) {
        throw IllegalStateException("it should has only one interface annotated with KtorfitRouterService,now is ${abstracts.size}")
    } else {
        abstracts[0]
    }
    return abstract
}

private fun getSatisfiedMethodFromAbstract(
    cls: KClass<*>, log: KLogger
) = cls.functions
    // excludes equals hashCode toString functions
    .filter {
        it.name !in setOf("equals", "hashCode", "toString")
    }
    // excludes not suspend function functions
    .filter {
        if (!it.isSuspend) {
            log.error { "find ${it.name} function is not suspend..., not suspend function is not support by Ktor" }
        }
        it.isSuspend
    }
    // excludes not annotated with Ktorfit Annotations
    .filter {
        isAnnotatedByKtorfit(it).also { result ->
            if (!result) {
                log.error {
                    """${it.name} function is not annotated with any Ktorfit anntotaions and it's not abstract,please check your code
                                        |if you have install Ktorfit correct,you might not to see this error,becase Ktorfit will prevent you to compile
                                        |this code,please check your Ktorfit installation,or maybe any functions in this interface are not annotated with Ktorfit annotations
                                    """.trimMargin()
                }
            }
        }
    }.toList()

private val ktorfitAnnotationsFqNameSet by lazy {
    listOf(GET::class, POST::class, PUT::class, DELETE::class, HEAD::class, OPTIONS::class, PATCH::class).mapNotNull {
        it.qualifiedName
    }.toSet()
}

private fun isAnnotatedByKtorfit(kFunction: KFunction<*>) = kFunction.annotations.filterKtorfitAnnotations().any()

private fun Collection<Annotation>.filterKtorfitAnnotations() =
    filter { it.annotationClass.qualifiedName in ktorfitAnnotationsFqNameSet }

data class HttpMethodUrl(val url: String, val method: HttpMethod)

data class HttpMethodDescriptor(val method: KFunction<*>, val httpMethodUrls: List<HttpMethodUrl>)

private fun parseAnnotation(annotation: Annotation): HttpMethodUrl {
    return when (annotation) {
        is GET -> HttpMethodUrl(annotation.value, HttpMethod.Get)
        is POST -> HttpMethodUrl(annotation.value, HttpMethod.Post)
        is PUT -> HttpMethodUrl(annotation.value, HttpMethod.Put)
        is DELETE -> HttpMethodUrl(annotation.value, HttpMethod.Delete)
        is HEAD -> HttpMethodUrl(annotation.value, HttpMethod.Head)
        is OPTIONS -> HttpMethodUrl(annotation.value, HttpMethod.Options)
        is PATCH -> HttpMethodUrl(annotation.value, HttpMethod.Patch)
        else -> shouldHappen()
    }
}

private fun Collection<KFunction<*>>.filterUserDefinedMethod() = filter {
    it.name !in setOf("equals", "hashCode", "toString")
}

private fun findMethod(
    implMethods: List<KFunction<*>>, httpMethodDescriptor: HttpMethodDescriptor
) = checkNotNull(implMethods.find { it.name == httpMethodDescriptor.method.name })




