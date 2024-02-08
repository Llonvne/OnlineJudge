package cn.llonvne.compoent

import cn.llonvne.entity.types.ProblemStatus
import cn.llonvne.entity.types.ProblemStatus.*
import io.kvision.core.Col
import io.kvision.core.Color
import io.kvision.core.Container
import io.kvision.html.span

fun Container.problemStatus(status: ProblemStatus) = span {
    when (status) {
        NotLogin -> {
            +"未登入"
        }

        NotBegin -> {
            +"未作答"
        }

        Accepted -> {
            +status.name
            color = Color.name(Col.GREEN)
        }

        WrongAnswer -> {
            +status.name
            color = Color.name(Col.RED)
        }
    }
}