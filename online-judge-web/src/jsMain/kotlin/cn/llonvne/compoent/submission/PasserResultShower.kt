package cn.llonvne.compoent.submission

import cn.llonvne.compoent.badges
import cn.llonvne.entity.problem.context.passer.PasserResult
import cn.llonvne.model.RoutingModule
import cn.llonvne.site.BooleanPasserResultDisplay
import io.kvision.core.onClickLaunch
import io.kvision.modal.Dialog

interface PasserResultShower {

    suspend fun show()

    companion object {
        fun from(passerResult: PasserResult, codeId: Int): PasserResultShower {
            return when (passerResult) {
                is PasserResult.BooleanResult -> BooleanResultPasserResultShower(
                    codeId, passerResult
                )
            }
        }
    }
}


private class BooleanResultPasserResultShower(
    private val codeId: Int,
    private val passerResult: PasserResult.BooleanResult
) :
    PasserResultShower {

    val booleanPasserResultDisplay = BooleanPasserResultDisplay(passerResult)

    val dialog = Dialog<Unit>(caption = "评测结果") {
        booleanPasserResultDisplay.load(this)

        badges {
            add {
                +"详情"

                onClickLaunch {
                    RoutingModule.routing.navigate("/share/$codeId")
                }
            }
        }
    }

    override suspend fun show() {
        dialog.show()
    }
}