package cn.llonvne.site.share.visisbility

import cn.llonvne.dtos.CodeDto
import cn.llonvne.entity.problem.ShareCodeComment
import cn.llonvne.kvision.service.CommentNotFound
import cn.llonvne.kvision.service.ICodeService.SetCodeCommentVisibilityTypeResp.SuccessSetCodeCommentVisibilityType
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.message.Messager
import cn.llonvne.model.CodeModel

class  CodeCommentVisibilityTypeChanger(override val codeDto: CodeDto) : VisibilityChanger {
    fun change(commentId: Int) {
        if (!isCodeOwner()) {
            return
        }

        change("更改该评论的可见性", ShareCodeComment.Companion.ShareCodeCommentType.entries) { type ->
            when (val resp = CodeModel.setCodeCommentVisibilityType(
                shareId = codeDto.codeId, commentId = commentId, type
            )) {        
                CommentNotFound -> return@change Messager.toastInfo("未找到该评论")
                PermissionDenied -> return@change Messager.toastInfo("权限不足")
                SuccessSetCodeCommentVisibilityType -> return@change Messager.toastInfo(
                    "修改成功"
                )
            }
        }
    }
}