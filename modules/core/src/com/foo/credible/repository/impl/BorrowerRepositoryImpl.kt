/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository.impl

import java.util.UUID
import javax.inject.Inject
import org.springframework.stereotype.Repository
import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.dto.BorrowerDto
import com.anzi.credible.entity.Borrower
import com.anzi.credible.entity.Institution
import com.anzi.credible.repository.BorrowerRepository
import com.anzi.credible.repository.QueryConstants
import com.anzi.credible.utils.DateUtils.toUTC
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.LoadContext

@Repository
open class BorrowerRepositoryImpl : BorrowerRepository {
    @Inject
    private lateinit var dataManager: DataManager

    /**
     * fetch Borrower entity from its primary key
     *
     * @param id
     * @return
     */
    override fun fetchBorrowerById(id: String): Borrower = dataManager.load(Borrower::class.java)
        .id(UUID.fromString(id))
        .view(ViewConstants.BORROWER_FETCH).one()

    /**
     * Persist a Borrower entity
     *
     * @param institution
     * @param borrowerDto
     * @return
     */
    override fun createBorrower(institution: Institution, borrowerDto: BorrowerDto): Borrower {
        val loadContext = LoadContext.create(Borrower::class.java)
            .setQuery(
                LoadContext.createQuery(QueryConstants.FETCH_BY_BORROWER_NAME)
                    .setParameter(
                        "name",
                        borrowerDto.name as String
                    )
            ).setView(ViewConstants.BORROWER_FETCH)

        val borrower = dataManager.load(loadContext) ?: let {
            dataManager.create(Borrower::class.java)
        }
        createBorrower(borrower, borrowerDto, institution)
        dataManager.commit(borrower)
        return borrower
    }

    /**
     * Updates Borrower entity
     *
     * @param borrower
     * @return
     */
    override fun updateBorrower(borrower: Borrower): Borrower = dataManager.commit(borrower)

    private fun createBorrower(
        borrower: Borrower,
        borrowerDto: BorrowerDto,
        institutionObj: Institution
    ) {
        borrower.apply {
            institution = institutionObj
            submissionDefaults = borrowerDto.submissionDefaults!!.toString()
            name = borrowerDto.name
            anzsic = borrowerDto.anzsic
            businessUnit = borrowerDto.businessUnit
            cadLevel = borrowerDto.cadLevel
            ccrRiskScore = borrowerDto.ccrRiskScore
            customerType = borrowerDto.customerType
            marketCap = borrowerDto.marketCap
            cadLevel = borrowerDto.cadLevel
            customerGroup = borrowerDto.customerGroup
            businessUnit = borrowerDto.businessUnit
            securityIndex = borrowerDto.securityIndex
            externalRatingAndOutLook = borrowerDto.externalRatingAndOutLook
            lastFullReviewAt = borrowerDto.lastFullReviewAt!!.toUTC()
            lastScheduleReviewAt = borrowerDto.lastScheduleReviewAt!!.toUTC()
            nextScheduleReviewAt = borrowerDto.nextScheduleReviewAt!!.toUTC()
            riskSignOff = borrowerDto.riskSignOff
            regulatoryRequirements = borrowerDto.regulatoryRequirements
        }
    }
}
