package cn.llonvne.site.share.visisbility

import cn.llonvne.dtos.CodeDto
import cn.llonvne.entity.problem.share.CodeVisibilityType
import cn.llonvne.message.Messager
import cn.llonvne.model.CodeModel
import io.kvision.html.p

class CodeVisibilityChanger(override val codeDto: CodeDto) : VisibilityChanger {
    fun change() {
        if (!isCodeOwner()) {
            return
        }

        change("更改代码可见性",
            choice = CodeVisibilityType.entries,
            slot = {
                if (codeDto.visibilityType == CodeVisibilityType.Restrict) {
                    p {
                        +"请注意你现在处于 ${codeDto.visibilityType.reprName} 模式中，一旦你更换到 公开/私密模式，将立刻使得目前的哈希链接失效，且不可恢复，如果你尝试再次切换到受限模式，将会生成一个新的哈希链接，并且使得旧的失效"
                    }
                }
            }
        ) { result ->
            when (val resp = CodeModel.setCodeVisibility(codeDto.codeId, result)) {
                cn.llonvne.kvision.service.CodeNotFound -> Messager.toastInfo("该分享代码不存在，或已被删除")
                cn.llonvne.kvision.service.PermissionDenied -> Messager.toastInfo("你未登入，或者不是改代码所有者无法更改可见性")
                cn.llonvne.kvision.service.ICodeService.SetCodeVisibilityResp.SuccessToPublicOrPrivate -> Messager.toastInfo(
                    "成功更改代码可见性"
                )

                is cn.llonvne.kvision.service.ICodeService.SetCodeVisibilityResp.SuccessToRestrict -> Messager.toastInfo(
                    "成功更改为受限类型,链接为${resp.link}"
                )
            }
        }
    }
}