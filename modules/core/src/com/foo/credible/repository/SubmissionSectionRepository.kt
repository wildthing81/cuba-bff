/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository

import com.anzi.credible.entity.Submission
import com.fasterxml.jackson.databind.JsonNode

interface SubmissionSectionRepository {

    fun fetchSectionsById(submissionId: String): Submission

    fun createSections(sections: List<JsonNode>): Submission
}
