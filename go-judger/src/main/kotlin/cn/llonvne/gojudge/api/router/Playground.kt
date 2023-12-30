package cn.llonvne.gojudge.api.router

import kotlinx.html.*

internal fun BODY.playground(languageName: String, judgePath: String = languageName) {
    h1 {
        +"$languageName Playground"
    }

    form {
        method = FormMethod.post
        action = judgePath

        label {
            htmlFor = "code"
            +"Your Code here"
        }

        br { }

        textArea {
            id = "code"
            required = true
            cols = "30"
            rows = "10"
            name = "code"
        }

        label {
            htmlFor = "stdin"
            +"Your stdin here"
        }

        br { }

        textArea {
            id = "stdin"
            required = false
            name = "stdin"
            cols = "30"
            rows = "10"
        }

        input {
            type = InputType.submit
            value = "Submit"
        }
    }
}
