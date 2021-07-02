/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository.impl

import javax.inject.Inject
import org.springframework.stereotype.Repository
import com.anzi.credible.constants.ViewConstants.BORROWER_LIST
import com.anzi.credible.entity.Borrower
import com.anzi.credible.entity.Institution
import com.anzi.credible.repository.InstitutionRepository
import com.anzi.credible.repository.QueryConstants.FETCH_BY_SITEID
import com.github.slugify.Slugify
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.entity.contracts.Id
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.LoadContext
import mu.KotlinLogging

@Repository
open class InstitutionRepositoryImpl : InstitutionRepository {

    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var persistence: Persistence

    @Inject
    private lateinit var dataManager: DataManager

    override fun fetchSiteConfigBySiteId(siteId: String): Institution? {
        var result: Any?
        persistence.createTransaction().use {
            val em: EntityManager = persistence.entityManager
            result = em.createQuery(FETCH_BY_SITEID).setParameter("siteId", siteId).firstResult
            it.commit()
            log.info { "query result: $result" }
        }
        return result as Institution
    }

    override fun createSiteConfig(name: String, configuration: String, borrowerDefaults: String): Institution? {
        val siteId = Slugify().slugify(name)
        log.info { "site id for Institution $name: $siteId" }
        val loadContext = LoadContext.create(Institution::class.java)
            .setQuery(
                LoadContext.createQuery(FETCH_BY_SITEID)
                    .setParameter(
                        "siteId",
                        siteId
                    )
            )

        val institution = dataManager.load(loadContext) ?: let {
            dataManager.create(Institution::class.java)
        }

        institution.name = name
        institution.siteId = siteId
        institution.configuration = configuration
        institution.borrowerDefaults = borrowerDefaults
        return dataManager.commit(institution)
    }

    override fun fetchBorrowers(institution: Institution): MutableSet<Borrower>? = dataManager
        .load(Id.of(institution))
        .view(BORROWER_LIST)
        .one().borrowers
}
