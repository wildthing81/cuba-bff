/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository

import com.anzi.credible.entity.Borrower
import com.anzi.credible.entity.Institution

interface InstitutionRepository {

    /**
     * TODO
     *
     * @param siteId
     * @return
     */
    fun fetchSiteConfigBySiteId(siteId: String): Institution?

    /**
     * Create Institution specific configuration for Submissions
     *
     * @param name
     * @param configuration
     * @param borrowerDefaults
     * @return
     */
    fun createSiteConfig(name: String, configuration: String, borrowerDefaults: String): Institution?

    /**
     * Fetch All borrowers under an Institution
     *
     * @param institution
     * @return list of Borrowers
     */
    fun fetchBorrowers(institution: Institution): MutableSet<Borrower>?
}
