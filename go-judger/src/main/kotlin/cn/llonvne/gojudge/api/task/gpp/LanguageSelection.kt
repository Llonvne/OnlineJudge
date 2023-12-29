package cn.llonvne.gojudge.api.task.gpp

import kotlinx.html.*
@HtmlTagMarker
fun FlowContent.languageSelection(
    languages: List<String>, legend: String = "请选择编程语言"
) = div {
    fieldSet {
        legend {
            +legend
        }

        div {
            languages.forEach { language ->
                input {
                    type = InputType.radio
                    id = language
                    name = "lang"
                    value = language
                }

                label {
                    htmlFor = language
                    +language
                }
            }
        }
    }
}