/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import com.anzi.credible.converters.StringToJsonBConverter
import com.haulmont.cuba.core.entity.StandardEntity
import com.haulmont.cuba.core.entity.annotation.OnDelete
import com.haulmont.cuba.core.global.DeletePolicy
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

@Table(name = "borrower")
@javax.persistence.Entity(name = "Borrower")
open class Borrower : StandardEntity() {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    var institution: Institution? = null

    @NotNull
    @Column(name = "name", nullable = false, unique = true)
    var name: String? = null

    @Lob
    @Column(name = "submission_defaults", columnDefinition = "jsonb")
    @Convert(converter = StringToJsonBConverter::class)
    var submissionDefaults: String? = null

    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "borrower")
    var submissions: MutableList<Submission>? = mutableListOf()

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "BORROWER_WATCHERS",
        joinColumns = [JoinColumn(name = "borrower_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var watchers: MutableSet<AppUser>? = mutableSetOf()

    @Column(name = "customer_type")
    var customerType: String? = null

    @Column(name = "market_cap")
    var marketCap: Long? = null

    @Column(name = "cad_level")
    var cadLevel: Int? = null

    @Column(name = "customer_group")
    var customerGroup: String? = null

    @Column(name = "business_unit")
    var businessUnit: String? = null

    @Column(name = "anzsic")
    var anzsic: String? = null

    @Column(name = "ccr_risk_score")
    var ccrRiskScore: Int? = null

    @Column(name = "security_index")
    var securityIndex: String? = null

    @Column(name = "external_rating_and_outlook")
    var externalRatingAndOutLook: Int? = null

    @Column(name = "last_full_review_ts")
    var lastFullReviewAt: Date? = null

    @Column(name = "last_schedule_review_ts")
    var lastScheduleReviewAt: Date? = null

    @Column(name = "next_schedule_review_ts")
    var nextScheduleReviewAt: Date? = null

    @Column(name = "risk_sign_off")
    var riskSignOff: String? = null

    @Column(name = "regulatory_requirements")
    var regulatoryRequirements: String? = null

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "BORROWER_TEAM",
        joinColumns = [JoinColumn(name = "borrower_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var team: MutableSet<AppUser> = mutableSetOf()

    companion object {
        private const val serialVersionUID = 3737641758891252926L
    }
}
