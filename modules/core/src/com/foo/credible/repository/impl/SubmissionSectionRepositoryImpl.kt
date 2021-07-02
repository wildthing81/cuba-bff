/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository.impl

import org.springframework.stereotype.Repository
import com.anzi.credible.entity.Submission
import com.anzi.credible.repository.SubmissionSectionRepository
import com.fasterxml.jackson.databind.JsonNode

@Repository
open class SubmissionSectionRepositoryImpl : SubmissionSectionRepository {

    override fun fetchSectionsById(submissionId: String): Submission {
        TODO("Not yet implemented")
    }

    override fun createSections(sections: List<JsonNode>): Submission {
        TODO("Not yet implemented")
    }
}
