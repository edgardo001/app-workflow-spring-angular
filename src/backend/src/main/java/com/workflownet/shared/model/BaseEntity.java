package com.workflownet.shared.model

import org.springframework.data.annotation.Id
import java.time.LocalDateTime

abstract class BaseEntity {
    @Id
    var id: String? = null
    var createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()
}
