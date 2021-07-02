package com.foo.credible.service

import com.anzi.credible.dto.WorkflowDto

interface WorkflowService {
    companion object {
        const val NAME = "crd_WorkflowService"
    }

    fun createWorkFlow(workflowDto: WorkflowDto): Any?

    fun fetchWorkFlows(): Any
}
