package cn.llonvne.constants

interface Site {
    val uri: String
    val name: String
}

object Frontend {
    object Index : Site {
        override val uri = "/"
        override val name: String = "主页"
    }

    object Problems : Site {
        override val uri = "/problems"
        override val name: String = "问题"

        object Create : Site {
            override val uri: String = "${Problems.uri}/create"
            override val name: String = "创建问题"
        }
    }

    object Login : Site {
        override val uri = "/login"
        override val name: String = "登入"
    }

    object Register : Site {
        override val uri = "/register"
        override val name: String = "注册"
    }

    object Mine : Site {
        override val uri: String = "/me"
        override val name: String = "我的"
    }
}



