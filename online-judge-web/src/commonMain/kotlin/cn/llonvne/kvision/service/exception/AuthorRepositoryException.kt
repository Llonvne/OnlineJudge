package cn.llonvne.kvision.service.exception

import cn.llonvne.JvmMainException
import io.kvision.annotations.KVServiceException
import io.kvision.remote.AbstractServiceException
import io.kvision.remote.ServiceException

@KVServiceException
class AuthorAuthenticationUserIdNotExist : AbstractServiceException() {
    override val message = "作者的注册用户ID不存在"
}

open class ProblemServiceException(msg: String) : JvmMainException(msg)

class ProblemIdDoNotExistAfterCreation : ProblemServiceException("题目ID在创建后仍不存在")

@KVServiceException
class AuthorNotExist : AbstractServiceException()

