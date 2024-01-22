package cn.llonvne.site.share

import cn.llonvne.AppScope
import cn.llonvne.kvision.service.ICodeService
import cn.llonvne.model.CodeModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

interface CodeLoader<ID> {
    fun load(id: ID): Deferred<ICodeService.GetCodeResp>

    companion object {
        fun id() = object : CodeLoader<Int> {
            override fun load(id: Int): Deferred<ICodeService.GetCodeResp> {
                return AppScope.async {
                    CodeModel.getCode(id)
                }
            }
        }

        fun hash() = object : CodeLoader<String> {
            override fun load(id: String): Deferred<ICodeService.GetCodeResp> {
                return AppScope.async {
                    CodeModel.getCode(id)
                }
            }
        }
    }
}