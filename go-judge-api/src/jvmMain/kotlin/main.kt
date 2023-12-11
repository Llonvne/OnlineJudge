import cn.llonvne.gojudge.api.*
import cn.llonvne.gojudge.api.gojudgespec.GoJudgeEnvSpec
import cn.llonvne.gojudge.api.gojudgespec.httpAddr
import cn.llonvne.gojudge.api.gojudgespec.url

fun main() {
    val me = Person(
        "Alejandro", 35,
        Address(Street("Kotlinstraat", 1), City("Hilversum", "Netherlands"))
    )

    GoJudgeEnvSpec.httpAddr.url
}