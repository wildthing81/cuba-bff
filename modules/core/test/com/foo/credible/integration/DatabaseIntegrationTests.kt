package com.foo.credible.integration

import java.io.File
import java.sql.Timestamp
import java.util.UUID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.RegisterExtension
import com.anzi.credible.CoreTestContainer.Common
import com.anzi.credible.TestConstants
import com.anzi.credible.dto.ActionDto
import com.anzi.credible.dto.ActivityDto
import com.anzi.credible.dto.BorrowerDto
import com.anzi.credible.dto.KeyValueDto
import com.anzi.credible.dto.SubmissionDto
import com.anzi.credible.dto.TaskDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.dto.WorkflowDto
import com.anzi.credible.entity.Borrower
import com.anzi.credible.service.ActivityService
import com.anzi.credible.service.BorrowerService
import com.anzi.credible.service.InstitutionService
import com.anzi.credible.service.SubmissionService
import com.anzi.credible.service.TaskService
import com.anzi.credible.service.UserService
import com.anzi.credible.service.WorkflowService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.haulmont.cuba.core.entity.StandardEntity
import com.haulmont.cuba.core.entity.contracts.Id
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.security.auth.AuthenticationManager
import com.haulmont.cuba.security.auth.LoginPasswordCredentials
import com.haulmont.cuba.testsupport.TestUserSessionSource
import io.mockk.InternalPlatformDsl.toStr
import strikt.api.expect
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue

// See https://doc.cuba-platform.com/manual-7.2/integration_tests_mw.html

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class DatabaseIntegrationTests {

    companion object {
        private const val basePath = TestConstants.INTEGRATION_BASE

        private lateinit var userSessionSource: TestUserSessionSource
        private lateinit var authManager: AuthenticationManager
        private lateinit var dataManager: DataManager

        private lateinit var institutionService: InstitutionService
        private lateinit var borrowerService: BorrowerService
        private lateinit var workflowService: WorkflowService
        private lateinit var userService: UserService
        private lateinit var submissionService: SubmissionService
        private lateinit var taskService: TaskService
        private lateinit var activityService: ActivityService

        private lateinit var submissionId: UUID
        private lateinit var testUserAnna: UserDto
        private lateinit var testUserCarl: UserDto
        private lateinit var borrowerDtoBhp: BorrowerDto

        @JvmField
        @RegisterExtension
        val testContainer = Common.INSTANCE

        @BeforeAll
        @JvmStatic
        @Throws(Exception::class)
        fun beforeAll() {
            // CUBA services
            userSessionSource = AppBeans.get(TestUserSessionSource::class.java)
            authManager = AppBeans.get(AuthenticationManager::class.java)
            dataManager = AppBeans.get(DataManager::class.java)

            // Credible services
            institutionService = AppBeans.get(InstitutionService.NAME)
            borrowerService = AppBeans.get(BorrowerService.NAME)
            workflowService = AppBeans.get(WorkflowService.NAME)
            userService = AppBeans.get(UserService.NAME)
            submissionService = AppBeans.get(SubmissionService.NAME)
            taskService = AppBeans.get(TaskService.NAME)
            activityService = AppBeans.get(ActivityService.NAME)

            // Create a login session & store in TestUserSessionSource
            authManager.login(
                LoginPasswordCredentials().apply {
                    login = "admin"
                    password = "admin"
                }
            ).session.let { userSessionSource.userSession = it }
        }

        @AfterAll
        @JvmStatic
        @Throws(Exception::class)
        fun afterAll() {
            authManager.logout()
        }

        /**
         * org.testcontainers.containers.PostgreSQLContainer.java will cleanup
         * on Test class finish
         *
         * A secondary cleanup() for use between @Test methods
         *
         */
        fun cleanUp(ids: List<UUID?>, entityClass: Class<out StandardEntity>) {
            ids.forEach { dataManager.remove(Id.of(it, entityClass)) }
        }
    }

    @Test
    @Order(0)
    fun testPostgresIsRunning() {
        expect { Common.dbContainer.isRunning }
    }

    @Test
    @Order(1)
    fun fetchSiteConfigTest() {
        val siteConfig = institutionService.fetchSiteConfig()

        println(siteConfig[1].toStr())

        expect {
            that(siteConfig[1]).isNotEmpty()
        }
    }

    @Test
    @Order(2)
    fun createAndFetchWorkflowTest() {
        ObjectMapper().registerKotlinModule()
            .readValue(File("$basePath/data/workflows/default.json"), WorkflowDto::class.java).let {
                workflowService.createWorkFlow(it)
            }

        val result = workflowService.fetchWorkFlows()

        expect {
            that(result).isA<List<WorkflowDto>>() and {
                get { this.size }.isEqualTo(1)
                get { this[0].name }.isEqualTo("default-workflow")
            }
        }
    }

    @Test
    @Order(3)
    fun createAndFetchBorrowerTest() {
        val bhp = ObjectMapper().registerKotlinModule()
            .readValue(File("$basePath/data/borrowers/bhp.json"), BorrowerDto::class.java).let {
                borrowerService.createBorrower(it) as BorrowerDto
            }

        borrowerDtoBhp = borrowerService.fetchBorrower(bhp.id.toString()) as BorrowerDto

        expect {
            that(borrowerDtoBhp).isA<BorrowerDto>() and {
                get { id }.isA<UUID>()
                get { name }.isEqualTo("BHP Group")
            }
        }
    }

    @Test
    @Order(4)
    fun createUsersTest() {
        var userDto = ObjectMapper().registerKotlinModule()
            .readValue(File("$basePath/data/users/anna.json"), UserDto::class.java)
        testUserAnna = userService.createUser(userDto) as UserDto

        userDto = ObjectMapper().registerKotlinModule()
            .readValue(File("$basePath/data/users/carl.json"), UserDto::class.java)
        testUserCarl = userService.createUser(userDto) as UserDto

        val result = userService.fetchInstitutionUsers()

        expect {
            that(result).isA<List<UserDto>>() and {
                get { size }.isEqualTo(2)
                get { this[0].name }.isEqualTo("Anna Analyst")
                get { this[1].name }.isEqualTo("Carl Credit")
            }
        }
    }

    @Test
    @Order(5)
    fun createAndFetchSubmissionTest() {
        val rt = ObjectMapper().registerKotlinModule()
            .readValue(File("$basePath/data/borrowers/rt.json"), BorrowerDto::class.java).let {
                borrowerService.createBorrower(it) as BorrowerDto
            }

        // admin logs out and anna logs in
        authManager.logout()
        authManager.login(
            LoginPasswordCredentials().apply {
                login = "anna"
                password = "anna123"
            }
        ).session.let { userSessionSource.userSession = it }

        val created = ObjectMapper().registerKotlinModule()
            .readValue(File("$basePath/data/submissions/rt.json"), SubmissionDto::class.java).let {
                submissionService.createSubmission(it.copy(borrower = rt.id.toString())) as SubmissionDto
            }
        submissionId = created.id!!
        val result = submissionService.fetchSubmission(created.id.toString())
        expect {
            that(result).isA<SubmissionDto>() and {
                get { borrower }.isA<BorrowerDto>() and {
                    get { name }.isEqualTo("Rio Tinto")
                }
                get { types!!.size }.isEqualTo(1)
                get { types?.get(0) }.isEqualTo("Limit Increase")
                get { note }.isEqualTo("Review inline with existing credit policy")
                get { workflowStep }.isNull()
                get { workflowStepName }.isNull()
                get { status }.isEqualTo("drafting")
                get { creator?.name }.isEqualTo("Anna Analyst")
                get { isViewed }.isNull()
            }
        }
    }

    @Test
    @Order(6)
    fun fetchSubmissionListTest() {
        var result = submissionService.fetchAssignedSubmissions(null)

        expect {
            that(result).isA<List<SubmissionDto>>() and {
                get { size }.isEqualTo(1)
                get { this[0].borrower }.isA<BorrowerDto>() and {
                    get { name }.isEqualTo("Rio Tinto")
                }
                get { this[0].due }.isEqualTo("2021-03-14T00:40:09.456Z")
                get { this[0].types!!.size }.isEqualTo(1)
                get { this[0].types?.get(0) }.isEqualTo("Limit Increase")
                get { this[0].workflowStep }.isEqualTo(1)
                get { this[0].workflowStepName }.isEqualTo("Drafting")
                get { this[0].creator?.name }.isNull()
                get { this[0].isViewed }.isTrue()
            }
        }

        val status = submissionService
            .actionStepTransition((result as List<SubmissionDto>)[0].id.toString(), 2) as Boolean

        expect {
            that(status).isTrue()
        }

        result = borrowerService.fetchBorrowers()

        expect {
            that(result).isA<List<BorrowerDto>>() and {
                get { this.find { it.name == "Rio Tinto" } }.isNotNull() and {
                    get { submissionCount }.isEqualTo(1)
                }
            }
        }
    }

    @Test
    @Order(7)
    fun checkTransitionAndCreateTaskTest() {
        val submissions = submissionService.fetchAssignedSubmissions(null)

        expect {
            that(submissions).isA<List<SubmissionDto>>() and {
                get { this[0].workflowStep }.isEqualTo(2)
                get { this[0].workflowStepName }.isEqualTo("Credit Review")
            }
        }

        val assignee = ObjectMapper().registerKotlinModule()
            .readValue(File("$basePath/data/users/rachael.json"), UserDto::class.java).let {
                userService.createUser(it) as UserDto
            }

        expect {
            that(assignee).isA<UserDto>()
            that(assignee.id).isA<UUID>()
        }

        val result = ObjectMapper().registerKotlinModule()
            .readValue(File("$basePath/data/tasks/formal.json"), TaskDto::class.java).let {
                taskService.createTask(
                    it.copy(
                        submissionId = (submissions as List<SubmissionDto>)[0].id.toString(),
                        assignee = assignee.id.toString()
                    )
                )
            }

        expect {
            that(result).isA<TaskDto>() and {
                get { borrower }.isA<BorrowerDto>() and {
                    get { name }.isEqualTo("Rio Tinto")
                }
                get { category }.isEqualTo("decision")
                get { type }.isEqualTo("formal")
                get { this.status }.isEqualTo("pending")
                get { description }.isEqualTo("test task")
                get { creator?.name }.isEqualTo("Anna Analyst")
                get { isViewed }.isFalse()
            }
        }
    }

    @Test
    @Order(8)
    fun fetchAllTasksTest() {
        val tasks = taskService.fetchTasks(false, null)

        expect {
            that(tasks).isA<List<TaskDto>>() and {
                get { this[0].borrower }.isA<BorrowerDto>() and {
                    get { name }.isEqualTo("Rio Tinto")
                }
                get { this[0].category }.isEqualTo("decision")
                get { this[0].type }.isEqualTo("formal")
                get { this[0].status }.isEqualTo("pending")
                get { this[0].creator?.name }.isEqualTo("Anna Analyst")
                get { this[0].assignee }.isA<UserDto>() and {
                    get { name }.isEqualTo("Rachael Relationship")
                }
                get { this[0].isViewed }.isFalse()
            }
        }
    }

    @Test
    @Order(9)
    fun fetchActivitiesTest() {
        val activities = activityService.fetchActivities(
            submissionId.toString(),
            Timestamp.valueOf("2020-01-01 3:36:17"),
            Timestamp.valueOf("2028-01-01 3:36:17")
        )

        expect {
            that(activities).isA<List<ActivityDto>>() and {
                get { this.size }.isEqualTo(4)
                get { this[0].type }.isEqualTo("submission")
                get { this[0].message }.isEqualTo("Anna Analyst created this submission")
                get { this[1].type }.isEqualTo("submission")
                get { this[1].message }.isEqualTo("Anna Analyst updated this submission")
                get { this[2].type }.isEqualTo("submission")
                get { this[2].message }.isEqualTo("Anna Analyst updated this submission")
                get { this[3].type }.isEqualTo("decision")
                get { this[3].message }.isEqualTo(
                    "Anna Analyst assigned a formal decision to Rachael Relationship " +
                        "{action}"
                )
                get { this[3].action }.isA<ActionDto>() and {
                    this.apply {
                        get { label }.isEqualTo("View Formal Decision")
                        get { payload }.isA<List<KeyValueDto>>() and {
                            get { this.size }.isEqualTo(1)
                            get { this[0].key }.isEqualTo("id")
                            get { this[0].value }.isA<String>()
                        }
                    }
                }
            }
        }
    }

    @Test
    @Order(10)
    fun updateSubmissionTest() {
        val updateDto = SubmissionDto(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            team = mutableSetOf(testUserCarl.id.toString())
        )

        val result = submissionService.updateSubmission(submissionId.toString(), updateDto)

        val updatedSubmissionDto = submissionService.fetchSubmission(submissionId.toString()) as SubmissionDto

        expect {
            that(result).isA<CommitContext>()
            that(updatedSubmissionDto.team?.size).isEqualTo(2)
        }
    }

    @Test
    @Order(11)
    fun updateBorrower() {
        val updateBorrowerDto = BorrowerDto(null, null, team = mutableSetOf(testUserCarl.id.toString()))
        val result = borrowerService.updateBorrower(borrowerDtoBhp.id.toString(), updateBorrowerDto)

        expect {
            that(result).isA<Borrower>() and {
                get { this.team.size }.isEqualTo(1)
            }
        }
    }
}
