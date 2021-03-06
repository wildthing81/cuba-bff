insert
	into
	institution (id,
	"version",
	create_ts,
	created_by,
	update_ts,
	updated_by,
	delete_ts,
	deleted_by,
	"name",
	site_id,
	"configuration",
	"borrowerdefaults")
values ('6af46bde-c6d4-58e1-d129-ccb2847dc685',
1,
'2021-01-13 04:35:13.364',
'admin',
'2021-01-13 04:35:13.364',
null,
null,
null,
'ANZ Banking Group Limited',
'anz-banking-group-limited',
'{
    "jobs": [
        {
            "slug": "rm",
            "title": "Relationship Manager"
        },
        {
            "slug": "ra",
            "title": "Research Analyst"
        },
        {
            "slug": "co",
            "title": "Credit Officer"
        },
        {
            "slug": "po",
            "title": "Product Owner"
        }
    ],
    "exceptions": [
        {
            "area": "pricing-summary",
            "slug": "group-entities-rating",
            "title": "Rating of group entities without financial statements",
            "description": "Rating of group entities without financial statements"
        }
    ],
    "taskStatus": [
        {
            "slug": "complete",
            "title": "Complete"
        },
        {
            "slug": "incomplete",
            "title": "Incomplete"
        }
    ],
    "reviewStatus": {
        "formal": [
            {
                "slug": "endorsed",
                "title": "Endorsed"
            },
            {
                "slug": "rework",
                "title": "Rework required"
            },
            {
                "slug": "pending",
                "title": "Pending"
            },
            {
                "slug": "abstained",
                "title": "Abstained"
            }
        ],
        "informal": [
            {
                "slug": "approved",
                "title": "Approved"
            },
            {
                "slug": "rework",
                "title": "Rework required"
            },
            {
                "slug": "pending",
                "title": "Pending"
            },
            {
                "slug": "declined",
                "title": "Declined"
            }
        ]
    },
    "submissionTypes": [
        {
            "slug": "new-facility",
            "title": "New Facility"
        },
        {
            "slug": "limit-increase",
            "title": "Limit Increase"
        },
        {
            "slug": "limit-reduction",
            "title": "Limit Reduction"
        },
        {
            "slug": "risk-grading",
            "title": "Risk Grading"
        },
        {
            "slug": "annual-review",
            "title": "Annual Review"
        },
        {
            "slug": "other",
            "title": "Other"
        }
    ],
    "submissionStatus": [
        {
            "slug": "drafting",
            "title": "Drafting"
        },
        {
            "slug": "pending",
            "title": "Pending formal decision"
        },
        {
            "slug": "approved",
            "title": "Approved"
        },
        {
            "slug": "declined",
            "title": "Declined"
        },
        {
            "slug": "withdrawn",
            "title": "Withdrawn"
        }
    ],
    "submissionTemplate": [
        {
            "slug": "summary",
            "title": "Summary"
        },
        {
            "slug": "other-limits",
            "title": "Other Limits"
        },
        {
            "slug": "notional",
            "title": "Contingent / Other Liabilities  (not Grouped) / Notional Grouping"
        },
        {
            "slug": "decision",
            "title": "Decision Required"
        },
        {
            "slug": "facility-uncommitted",
            "title": "Comment on whether the facility is uncommitted and / or unadvised"
        },
        {
            "slug": "additional-item",
            "title": "Additional item for approving and noting"
        },
        {
            "slug": "transaction-overview",
            "title": "Transaction overview"
        },
        {
            "slug": "pricing-summary",
            "title": "Pricing Summary"
        },
        {
            "slug": "relationship",
            "title": "Relationship / Credit Strategy / Rationale"
        },
        {
            "slug": "terms",
            "title": "Key Terms & Conditions"
        },
        {
            "slug": "business-summary",
            "title": "Customer, Business & Industry Summary"
        },
        {
            "slug": "risk-grading",
            "title": "Risk Grading"
        },
        {
            "slug": "financial-analysis",
            "title": "Financial Analysis"
        },
        {
            "slug": "risks",
            "title": "Risks & Mitigants"
        },
        {
            "slug": "assumptions",
            "title": "Basis of Financial Analysis & Financial Forecast Assumptions"
        },
        {
            "slug": "conclusions",
            "title": "Conclusions on Financial Analysis - Historical"
        }
    ],
    "notifications": [
            {
                "type": "subAssignedMe",
                "description": "A submission was assigned to you"
            },
             {
                "type": "subPastDue",
                "description": "The due date passed on one of your submissions"
            },
            {
                "type": "subDecisionMade",
                "description": "A decision was made on one of your submissions"
            },
            {
                "type": "taskAssignedMe",
                "description": "A task was assigned to you"
            },
            {
                "type": "taskPastDueMe",
                "description": "The due date passed on a task assigned to you"
            },
            {
                "type": "taskPastDueOther",
                "description": "The due date passed on a task you assigned"
            }
    ]
}',
'{
    "cmf": [
        {
            "slug": "sample-1",
            "title": "Sample CMF 1"
        },
        {
            "slug": "sample-2",
            "title": "Sample CMF 2"
        }
    ],
    "anzsic": [
        {
            "slug": "nursery-production",
            "title": "Nursery Production (Under Cover)"
        },
        {
            "slug": "ice-cream-manufacturing",
            "title": "Ice Cream Manufacturing"
        },
        {
            "slug": "metal-product-manufacturing",
            "title": "Other Basic Non-Ferrous Metal Product Manufacturing"
        }
    ],
    "cadLevel": [
        {
            "slug": "rm-4",
            "limit": "100000000",
            "title": "Relationship Manager - Level 4"
        },
        {
            "slug": "rm-3",
            "limit": "50000000",
            "title": "Relationship Manager - Level 3"
        },
        {
            "slug": "rm-2",
            "limit": "1000000",
            "title": "Relationship Manager - Level 2"
        },
        {
            "slug": "rm-1",
            "limit": "500000",
            "title": "Relationship Manager - Level 1 Small Business Manager"
        },
        {
            "slug": "am",
            "limit": "250000",
            "title": "Banking Consultant / Assistant Manager"
        },
        {
            "slug": "am-cad",
            "limit": "150000",
            "title": "Banking Consultant / Assistant Manager - Excess only CAD"
        }
    ],
    "businessUnit": [
        {
            "slug": "business-banking",
            "title": "Business Banking"
        },
        {
            "slug": "institutional",
            "title": "Institutional"
        },
        {
            "slug": "private-banking",
            "title": "Private Banking"
        }
    ],
    "customerType": [
        {
            "slug": "individual",
            "title": "Individual"
        },
        {
            "slug": "sole-trader",
            "title": "Sole Trader"
        },
        {
            "slug": "partnership-regulated",
            "title": "Partnership - Regulated"
        },
        {
            "slug": "partnership-unregulated",
            "title": "Partnership - Unregulated"
        },
        {
            "slug": "co-operative",
            "title": "Co-operative"
        },
        {
            "slug": "association-incorporated",
            "title": "Association - Incorporated"
        },
        {
            "slug": "association-unincorporated",
            "title": "Association - Unincorporated"
        },
        {
            "slug": "trust-regulated",
            "title": "Trust - Regulated"
        },
        {
            "slug": "trust-regulated",
            "title": "Trust - Unregulated"
        },
        {
            "slug": "company-public",
            "title": "Company - Public"
        },
        {
            "slug": "company-private",
            "title": "Company - Private"
        },
        {
            "slug": "government",
            "title": "Government"
        }
    ],
    "customerGroup": [
        {
            "slug": "households",
            "title": "Households"
        },
        {
            "slug": "comm-service-org",
            "title": "Community service organisations"
        },
        {
            "slug": "pvt-non-financial-corp",
            "title": "Private non-financial corporations: Private non-financial investment funds - Real estate investment trusts (REITs)"
        }
    ],
    "individualRoles": [
        {
            "slug": "board-member",
            "title": "Board Member"
        },
        {
            "slug": "chairman",
            "title": "Chairman"
        },
        {
            "slug": "cao",
            "title": "Chief Analytics Officer"
        },
        {
            "slug": "cco",
            "title": "Chief Commercial Officer"
        },
        {
            "slug": "ccusto",
            "title": "Chief Customer Officer"
        },
        {
            "slug": "cdo",
            "title": "Chief Data Officer"
        }
    ]
}')
on conflict (id) do update set "configuration"= excluded."configuration", "borrowerdefaults"= excluded."borrowerdefaults";



update SEC_USER set institution_id = ( select id from institution where site_id = 'anz-banking-group-limited') where
sec_user.institution_id is null ;
