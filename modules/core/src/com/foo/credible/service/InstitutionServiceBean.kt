/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import javax.inject.Inject
import org.springframework.stereotype.Service
import com.anzi.credible.repository.InstitutionRepository
import mu.KotlinLogging

@Service(InstitutionService.NAME)
open class InstitutionServiceBean : InstitutionService {
    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var userService: UserService

    @Inject
    private lateinit var repository: InstitutionRepository

    /**
     * Fetch site(tenant) configuration from institution
     *
     * @param siteId
     * @return
     */
    override fun fetchSiteConfig(): Array<String> {
        val siteId = userService.fetchLoggedUser().institution!!.siteId
        return repository.fetchSiteConfigBySiteId(siteId!!)?.let {
            val siteArray = Array(3) { "" }
            siteArray[0] = it.name!!
            siteArray[1] = it.configuration!!
            siteArray[2] = it.borrowerDefaults!!
            siteArray
        } ?: run {
            log.error { "No configuration exists for Institution: $siteId" }
            emptyArray()
        }
    }

    /**
     * Create site(tenant) configuration in institution
     *
     * @param name
     * @param configuration
     * @return
     */
    override fun createSiteConfig(name: String, configuration: String, borrowerDefaults: String): String {
        val institution = repository.createSiteConfig(name, configuration, borrowerDefaults)
        return institution?.let {
            log.info { "Site configuration created for Institution: $name" }
            institution.siteId
        } ?: run {
            log.error(name) { "Unable to create site configuration for Institution: $name" }
            "Failure"
        }
    }
}
