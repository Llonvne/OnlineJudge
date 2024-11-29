package cn.llonvne.kvision.service

import org.springframework.stereotype.Service
import java.util.*

@Service
class GroupHashService {
    fun hash(): String = UUID.randomUUID().toString()
}
