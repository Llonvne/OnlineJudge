package cn.llonvne.site.problem.detail

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badges
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.GetProblemByIdOk
import cn.llonvne.ll
import io.kvision.core.Container
import io.kvision.html.h1
import io.kvision.html.p

fun interface DetailHeaderShower {
    fun show(root: Container)

    companion object {
        fun from(resp: GetProblemByIdOk): DetailHeaderShower {
            return AbstractDetailHeaderShower(resp)
        }
    }
}

private class AbstractDetailHeaderShower(result: GetProblemByIdOk) :
    DetailHeaderShower {
    protected val problem = result.problem

    override fun show(root: Container) {
        root.alert(AlertType.Light) {
            h1 {
                +problem.problemName
            }
            p {
                +problem.problemDescription
            }

            badges {
                add {
                    +"内存限制:${problem.memoryLimit}"
                }

                add {
                    +"时间限制:${problem.timeLimit}"
                }

                add {
                    +"可见性:${problem.visibility.chinese}"
                }

                add {
                    +"类型:${problem.type.name}"
                }

                add {
                    +"最后更新于:${problem.updatedAt?.ll()}"
                }
            }
        }
    }
}