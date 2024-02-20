package cn.llonvne.kvision.service

import cn.llonvne.kvision.service.IGroupService.CreateGroupResp

/**
 * 用户端异常
 * 用于描述用户造成的异常
 * @property message 异常信息可以发送给用户
 */
data class ClientError(val message: String = "") : CreateGroupResp