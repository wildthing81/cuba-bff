/*
 * The code is copyright ©2021
 */

package com.foo.credible

import com.haulmont.cuba.testsupport.TestContainer
import org.junit.jupiter.api.extension.ExtensionContext
import org.testcontainers.containers.PostgreSQLContainer

open class CoreTestContainer : TestContainer() {

    init {
        appComponents = listOf(
            "com.haulmont.cuba",
            "com.haulmont.addon.restapi"
        )
        appPropertiesFiles = mutableListOf(
            // List the files defined in your web.xml
            // in appPropertiesConfig context parameter of the core module
            "com/anzi/credible/app.properties",
            // Add this file which is located in CUBA and defines some properties
            // specifically for test environment. You can replace it with your own
            // or add another one in the end.
            "com/anzi/credible/test-app.properties"
        )
    }

    fun configureDataSource() {
        dbDriver = Common.dbContainer.driverClassName
        dbUrl = Common.dbContainer.jdbcUrl
        dbUser = Common.dbContainer.username
        dbPassword = Common.dbContainer.password
        // return super.autoConfigureDataSource()
    }

    class Common private constructor() : CoreTestContainer() {

        @Throws(Throwable::class)
        override fun beforeAll(extensionContext: ExtensionContext) {
            if (!initialized) {
                this.configureDataSource()
                super.beforeAll(extensionContext)
                initialized = true
            }

            setupContext()
        }

        override fun afterAll(extensionContext: ExtensionContext) {
            cleanupContext()
            // never stops - do not call super
        }

        companion object {

            // @Container
            val dbContainer = PostgreSQLContainer<Nothing>("postgres:11.7").apply {
                withDatabaseName("testdb")
                withUsername("credible")
                withPassword("credible")
                start()
            }

            val INSTANCE = Common()

            @Volatile
            private var initialized: Boolean = false
        }
    }
}
