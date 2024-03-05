package cn.llonvne.site.contest.detail

import cn.llonvne.compoent.alert
import cn.llonvne.compoent.badges
import cn.llonvne.kvision.service.IContestService
import cn.llonvne.ll
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.html.h4

interface ContestDetailHeader {
    fun show(container: Container)

    companion object {
        fun form(loadOk: IContestService.LoadContestResp.LoadOk): ContestDetailHeader =
            BaseContestDetailHeader(loadOk)
    }
}

private class BaseContestDetailHeader(private val loadOk: IContestService.LoadContestResp.LoadOk) :
    ContestDetailHeader {
    private val contestStatusResolver = ContestStatusResolver(loadOk.contest.startAt, loadOk.contest.endAt)
    override fun show(container: Container) {
        container.div {
            alert(contestStatusResolver.statusColor()) {
                h1 {
                    +loadOk.contest.title
                }

                h4(rich = true) {
                    +loadOk.contest.description
                }

                badges {
                    add {
                        +contestStatusResolver.status().name
                    }
                    add {
                        +"所有者:${loadOk.ownerName}"
                    }
                    add {
                        +loadOk.contest.contestScoreType.name
                    }
                    add {
                        +loadOk.contest.rankType.chinese
                    }
                    add {
                        +"开始于${loadOk.contest.startAt.ll()}"
                    }
                    add {
                        +"结束于${loadOk.contest.endAt.ll()}"
                    }
                }
            }
        }
    }
}