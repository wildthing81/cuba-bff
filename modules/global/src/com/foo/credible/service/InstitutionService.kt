/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

interface InstitutionService {
    companion object {
        const val NAME = "crd_InstitutionService"
    }

    fun fetchSiteConfig(): Array<String>

    fun createSiteConfig(name: String, configuration: String, borrowerDefaults: String): String
}
