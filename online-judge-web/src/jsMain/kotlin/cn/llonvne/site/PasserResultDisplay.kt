package cn.llonvne.site

import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badges
import cn.llonvne.entity.problem.context.passer.PasserResult
import cn.llonvne.model.RoutingModule
import io.kvision.core.Container
import io.kvision.core.onClick
import io.kvision.core.onClickLaunch
import io.kvision.html.h4

interface PasserResultDisplay {

    fun load(root: Container)

    companion object {
        fun from(passerResult: PasserResult): PasserResultDisplay {
            return when (passerResult) {
                is PasserResult.BooleanResult -> BooleanPasserResultDisplay(passerResult)
            }
        }
    }
}

class BooleanPasserResultDisplay(
    private val booleanResult: PasserResult.BooleanResult,
    private val codeId: Int? = null,
    private val small: Boolean = false
) :
    PasserResultDisplay {
    private fun onSmall(root: Container) {
        root.badges {
            add(booleanResult.suggestColor) {
                +booleanResult.readable
                onClickLaunch {
                    RoutingModule.routing.navigate("/share/$codeId")
                }
            }
        }
    }

    override fun load(root: Container) {
        if (small) {
            onSmall(root)
            return
        } else {
            if (booleanResult.result) {
                root.alert(AlertType.Success) {
                    +"Accepted"
                }
            } else {
                root.alert(AlertType.Danger) {
                    h4 {
                        +"Wrong Answer"
                    }
                }
            }
            if (codeId != null) {
                root.badges {
                    add {
                        +"详情"

                        onClick {
                            RoutingModule.routing.navigate("/share/$codeId")
                        }
                    }
                }
            }
        }
    }
}

