/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository

import com.anzi.credible.repository.impl.ActivityRepositoryImpl
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.global.DataManager
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityRepositoryImplTest {

    @MockK
    lateinit var entityManager: EntityManager

    @MockK
    lateinit var dataManager: DataManager

    @InjectMockKs
    var testRepository = ActivityRepositoryImpl()

    @BeforeAll
    fun setUp() {
    }

    // @Test
    fun testFetchActivityByIdSuccess() {
        TODO("CUBA method calls")
    }

    // @Test
    fun testFetchActivitiesBySubmissionAndTimeFrameSuccess() {
        TODO("CUBA method calls")
    }

    // @Test
    fun testCreateActivitySuccess() {
        TODO("CUBA method calls")
    }

    @AfterEach
    fun reset() {
        clearAllMocks()
    }

    @AfterAll
    fun tearDown() {
        unmockkAll()
    }
}
