/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import javax.inject.Inject
import org.springframework.stereotype.Service
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.BorrowerDto
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.entity.Borrower
import com.anzi.credible.entity.Institution
import com.anzi.credible.helpers.BorrowerHelper
import com.anzi.credible.repository.BorrowerRepository
import com.anzi.credible.repository.InstitutionRepository
import mu.KotlinLogging

@Service(BorrowerService.NAME)
open class BorrowerServiceBean : BorrowerService {
    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var userService: UserService

    @Inject
    private lateinit var institutionRepository: InstitutionRepository

    @Inject
    private lateinit var borrowerRepository: BorrowerRepository

    /**
     * Fetch borrower by Id.
     *
     * @param id
     * @return
     */
    override fun fetchBorrower(id: String) = try {
        val loggedUser = userService.fetchLoggedUser()
        log.debug { "Logged in user: ${loggedUser.login}" }
        BorrowerHelper.buildDto(borrowerRepository.fetchBorrowerById(id))
    } catch (dbe: Exception) {
        log.error { "No borrower exists for :$id" }
        ErrorDto(
            Borrower::class.simpleName!!,
            id,
            ErrorConstants.FIND_ENTITY,
            dbe.message
        )
    }

    /**
     * Fetch borrowers per institution
     *
     * @return
     */
    override fun fetchBorrowers(): Any {
        val institution = userService.fetchLoggedUser().institution!!
        return try {
            log.debug { "site Id: ${institution.siteId}" }
            institutionRepository.fetchBorrowers(institution)!!.map {
                BorrowerDto(it.id, it.name, submissionCount = it.submissions!!.size)
            }
        } catch (dbe: Exception) {
            log.error { "No borrowers exists for ${institution.siteId}" }
            ErrorDto(
                Institution::class.simpleName!!,
                institution.siteId,
                ErrorConstants.FIND_ENTITY,
                dbe.message
            )
        }
    }

    /**
     * Create Borrower
     *
     * @param borrowerDto
     * @return borrowerDto
     */
    override fun createBorrower(borrowerDto: BorrowerDto) = try {
        val institution = userService.fetchLoggedUser().institution
        log.info { "Creating a new borrower for institution: ${institution!!.siteId}" }
        borrowerRepository.createBorrower(institution!!, borrowerDto).let {
            log.info { "New borrower ${it.id}" }
            borrowerDto.copy(id = it.id)
        }
    } catch (dbe: Exception) {
        log.error(dbe) { "Error creating new borrower" }
        ErrorDto(Borrower::class.simpleName!!, null, ErrorConstants.CREATE_ENTITY, dbe.message)
    }

    /**
     * Updates borrower details
     *
     * @param borrowerId
     * @param borrowerDto
     */
    override fun updateBorrower(borrowerId: String, borrowerDto: BorrowerDto) = try {
        borrowerRepository.fetchBorrowerById(borrowerId).let { borrower ->
            borrowerDto.team?.let {
                borrower.team.addAll(userService.fetchUsersById(it.toList() as List<String>))
            }
            borrowerRepository.updateBorrower(borrower)
        }
    } catch (dbe: Exception) {
        log.error { "Error updating borrower: $borrowerId" }
        ErrorDto(
            Borrower::class.simpleName!!,
            borrowerId,
            ErrorConstants.UPDATE_ENTITY,
            dbe.message
        )
    }
}
