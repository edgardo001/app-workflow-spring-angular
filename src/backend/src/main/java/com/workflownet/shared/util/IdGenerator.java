package com.workflownet.shared.util

import java.util.UUID
import org.springframework.stereotype.Component

@Component
class IdGenerator {
    fun generate(): String = UUID.randomUUID().toString()
}
