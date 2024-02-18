package cn.llonvne.site.share

import cn.llonvne.AppScope
import cn.llonvne.kvision.service.ICodeService.GetCodeResp
import cn.llonvne.model.CodeModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

interface CodeLoader<ID> {
    fun load(id: ID): Deferred<GetCodeResp>

    companion object {
        fun id() = object : CodeLoader<Int> {
            override fun load(id: Int): Deferred<GetCodeResp> {
                return AppScope.async {
                    CodeModel.getCode(id)
                }
            }
        }

        fun hash() = object : CodeLoader<String> {
            override fun load(id: String): Deferred<GetCodeResp> {
                return AppScope.async {
                    CodeModel.getCode(id)
                }
            }
        }
    }
}