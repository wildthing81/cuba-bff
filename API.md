# Credible Backend API

Serves data to the **Credible UI** front end.

**Note**: Unless specified all requests use `Accepts: 'application/json', Content-Type: 'application/json'`

## Summary

### Authentication

- Login: `POST /v2/oauth/token`
- Logout: `POST /user/logout`
- Refresh Token: `POST /v2/oauth/token` (with basic auth ala login, and `grant_type=refresh_token&refresh_token={refresh_token}`)

### User

- My profile: `GET /user/me`
- Create User: `POST /user`
- List Users: `GET /users`

### Institutions

- Create Institution: `POST /institution`
- Institution Details: `GET /institution`

### Submissions

- List Submissions: `GET /submissions`
- Count Submissions: `GET /submissions/count`
- List Submissions I'm Watching: `GET /submissions/watching`
- Count Submissions I'm Watching: `GET /submissions/watching/count`
- Submission Detail: `GET /submission/:id`
- Create Submission: `POST /submission`
- Update Submission: `PATCH /submission/:id`
- Submission Activity: `GET /submission/:id/activities`
- Submission Comments: `GET /submission/:id/comments`

### Tasks

- List Tasks: `GET /tasks`
- Count Tasks: `GET /tasks/count`
- List Tasks Assigned To Me: `GET /tasks/assigned`
- Count Tasks Assigned To Me: `GET /tasks/assigned/count`
- Task Detail: `GET /task/:id`
- Create Task: `POST /task`
- Update Task: `PATCH /task/:id`

### Borrowers

- Create Borrower: `POST /borrower`
- List Borrowers: `GET /borrowers`
- Borrower Details: `GET /borrower/:id`

### Workflows

- Create Workflow: `POST /workflow`
- Try Workflow Changes: `GET /workflows/:id/publishApproval`
- Publish Workflow: `PATCH /workflow/:id/publish`
- Update Workflow: `PATCH /workflow/:id`
- Delete Workflow: `DELETE /workflow/:id`
- List Workflows: `GET /workflows`

### Notifications

- List Notifications: `GET /notifications`

### News

- List News Items: `GET /news`

### Comments

- Create Comment: `PUT /submission/:id/comment/:cid`
- Get Comment: `GET /submission/:id/comment/:cid`

See below for more details.

---

## AUTHENTICATION

### Log in and get a token

The `Authorization` header used at initial login is the `base64` encoded concatenated `username` and `password` for the system (eg `'credible-ui:secret'`)

See also [RFC 7617: Basic Authentication](https://tools.ietf.org/html/rfc7617).

`POST /v2/oauth/token`

```text
POST /v2/oauth/token (Authorization: Basic XXXX, Content-Type: application/x-www-form-urlencoded) username=aaa&password=bbb&grant_type=password => returns token data as per

{
  "access_token": "56c1e5c2-77bb-43dc-84cc-a4b071c4d771",
  "token_type": "bearer",
  "refresh_token": "fc9f02a4-7645-467e-a7bd-0cad7b230b7a",
  "expires_in": 3599,
  "scope": "rest-api"
}
```

### Log out

`POST /user/logout`

```text
POST /user/logout => removes user session/access tokens and returns 200 Ok
```

### Refresh token

The `Authorization` header used when refreshing the token is the `base64` encoded concatenated `username` and `password` for the system (eg `'credible-ui:secret'`) as per the `login` route.

`POST /v2/oauth/token`

```text
POST /v2/oauth/token (Authorization: Basic XXXX, Content-Type: application/x-www-form-urlencoded) grant_type=refresh_token&refresh_token=xxx => returns refreshed token data as per

{
  "access_token": "56c1e5c2-77bb-43dc-84cc-a4b071c4d771",
  "token_type": "bearer",
  "refresh_token": "fc9f02a4-7645-467e-a7bd-0cad7b230b7a",
  "expires_in": 3599,
  "scope": "rest-api"
}
```

## Authenticated endpoints

From this point on **all** requests use the `access_token` in the `Authorization` header as a `Bearer` token.

```yml
Authorization: Bearer access_token
```

### Get your own user data

`GET /user/me`

```text
GET /user/me

{
  id,
  login,
  name,
  position,
  email,
  roles,
  scope,
  cadLevel,
  profileImage: 'https://randomuser.me/api/portraits/women/71.jpg', // url or perhaps base64 encoded image
  preferences: [
    {
      key: string, // scope of preferences tdb. example is the user's preferred dashboard tab.
      value: string
    }
  ]
}
```

---

## INSTITUTIONS

### Create an institution (admin only)

`POST /institution`

```text
POST /institution => returns 201 Created and { slug } (must have admin role to do this)

{
  name: 'ANZ Banking Corporation Limited',
  configuration: {
    // use array of { slug, title } pairs to be compliant with OpenAPI, given the actual jobs could vary between institutions
  jobs: [
    { slug: 'rm', title: 'Relationship Manager' },
    { slug: 'ra', title: 'Research Analyst' },
    { slug: 'co', title: 'Credit Officer' },
    { slug: 'po', title: 'Product Owner' }
  ],
  submissionTypes: [
    { slug: 'new-facility', title: 'New Facility' },
    { slug: 'limit-increase', title: 'Limit Increase' },
    { slug: 'limit-reduction', title: 'Limit Reduction' },
    { slug: 'risk-grading', title: 'Risk Grading' },
    { slug: 'approval-review', title: 'Approval Review' },
    { slug: 'other', title: 'Other' }
  ],
  submissionTemplate: [
    { slug: 'summary', title: 'Summary' },
    { slug: 'other-limits', title: 'Other Limits' },
    { slug: 'notional', title: 'Contingent / Other Liabilities  (not Grouped) / Notional Grouping' },
    { slug: 'decision', title: 'Decision Required' },
    {
      slug: 'facility-uncommitted',
      title: 'Comment on whether the facility is uncommitted and / or unadvised'
    },
    { slug: 'additional-item', title: 'Additional item for approving and noting' },
    { slug: 'transaction-overview', title: 'Transaction overview' },
    { slug: 'pricing-summary', title: 'Pricing Summary' },
    { slug: 'relationship', title: 'Relationship / Credit Strategy / Rationale' },
    { slug: 'terms', title: 'Key Terms & Conditions' },
    { slug: 'business-summary', title: 'Customer, Business and Industry Summary' },
    { slug: 'risk-grading', title: 'Risk Grading' },
    { slug: 'financial-analysis', title: 'Financial Analysis' },
    { slug: 'risks', title: 'Risks & Mitigants' },
    { slug: 'assumptions', title: 'Basis of Financial Analysis & Financial Forecast Assumptions' },
    { slug: 'conclusions', title: 'Conclusions on Financial Analysis - Historical' }
  ],
  submissionStatus: [
    { slug: 'drafting', title: 'Drafting' },
    { slug: 'pending', title: 'Pending formal decision' },
    { slug: 'approved', title: 'Approved' },
    { slug: 'declined', title: 'Declined' },
    { slug: 'withdrawn', title: 'Withdrawn' }
  ],
  reviewStatus: {
    // if a submission's workflow step is 'Credit review' then only allow formal reviews
    // QUESTION: given the new workflow structure, how do we enforce this?
    formal: [
      { slug: 'endorsed', title: 'Endorsed' },
      { slug: 'rework', title: 'Rework required' },
      { slug: 'pending', title: 'Pending' },
      { slug: 'abstained', title: 'Abstained' }
    ],
    // if a submission's workflow step is 'Drafting' then only allow informal reviews
    // QUESTION: given the new workflow structure, how do we enforce this?
    informal: [
      { slug: 'approved', title: 'Approved' },
      { slug: 'rework', title: 'Rework required' },
      { slug: 'pending', title: 'Pending' },
      { slug: 'declined', title: 'Declined' }
    ]
  },
  taskStatus: [
    { slug: 'complete', title: 'Complete' },
    { slug: 'incomplete', title: 'Incomplete' }
  ],
  exceptions: [
    {
      slug: 'group-entities-rating',
      title: 'Rating of group entities without financial statements',
      area: 'pricing-summary',
      description: 'Rating of group entities without financial statements'
    }
  ],
  borrowerDefaults: {
     customerType: [
        // This may not be the full list. Sample data only
        { slug: 'individual', title: 'Individual' },
        { slug: 'sole-trader', title: 'Sole Trader' },
        { slug: 'partnership-regulated', title: 'Partnership - Regulated' },
        { slug: 'partnership-unregulated', title: 'Partnership - Unregulated' }, 
        { slug: 'co-operative', title: 'Co-Operative' },
        { slug: 'association-incorporated', title: 'Association - Incorporated' },
        { slug: 'association-unincorporated', title: 'Association - Unincorporated' },
        { slug: 'trust-regulated', title: 'Trust - Regulated' },
        { slug: 'trust-regulated, title: 'Trust - Unregulated' },
        { slug: 'company-public', title: 'Company - Public' },
        { slug: 'company-private', title: 'Company - Private' },
        { slug: 'government', title: 'Government' }
     ],
     customerGroup: [
        // This may not be the full list. Sample data only
        { slug: 'households', title: 'Households' },
        { slug: 'comm-service-org', title: 'Community service organisations' },
        { slug: 'pvt-non-financial-corp', title: 'Private non-financial corporations: Private non-financial investment funds - Real estate investment trusts (REITs)' }
     ],
     cadLevel: [
        // This may not be the full list. Sample data only
        { slug: 'rm-4', title: 'Relationship Manager - Level 4', limit: number // dollar amount },
        { slug: 'rm-3', title: 'Relationship Manager - Level 3', limit: number // dollar amount },
        { slug: 'rm-2', title: 'Relationship Manager - Level 2', limit: number // dollar amount },
        { slug: 'rm-1', title: 'Relationship Manager - Level 1 Small Business Manager', limit: number // dollar amount },
        { slug: 'am', title: 'Banking Consultant / Assistant Manager', limit: number // dollar amount },
        { slug: 'am-cad', title: 'Banking Consultant / Assistant Manager - Excess only CAD', limit: number // dollar amount }, 
     ],
     cmf: [
        // This may not be the full list. Sample data only
        { slug: 'sample-1', title: 'Sample CMF 1' },
        { slug: 'sample-2', title: 'Sample CMF 2' }
     ],
     businessUnit: [
        // This may not be the full list. Sample data only
        { slug: 'business-banking', title: 'Business Banking' },
        { slug: 'institutional', title: 'Institutional' },
        { slug: 'private-banking', title: 'Private Banking' }
     ],
     anzsic: [
        // This may not be the full list. Sample data only
        { slug: 'nursery-production', title: 'Nursery Production (Under Cover)' },
        { slug: 'ice-cream-manufacturing',  title: 'Ice Cream Manufacturing' },
        { slug: 'metal-product-manufacturing',  title: 'Other Basic Non-Ferrous Metal Product Manufacturing' }
     ],
     individualRoles: [
        // This may not be the full list. Sample data only
        { slug: 'board-member', title: 'Board Member' }, 
        { slug: 'chairman', title: 'Chairman' },
        { slug: 'cao', title: 'Chief Analytics Officer' },
        { slug: 'cco', title: 'Chief Commercial Officer' },
        { slug: 'ccusto', title: 'Chief Customer Officer' },
        { slug: 'cdo', title: 'Chief Data Officer' }
     ]
  }
}
```

### Get the user's own institution data

`GET /institution`

```text
GET /institution => 200 OK and details of the user's institution.

{
  id: 'aabbccdd', // the uuid of the institution
  name: 'ANZ Banking Corporation Limited',
  slug: 'anz', // either supplied or can be created based on the name - should be short, must be unique
  configuration: {
      // use array of { slug, title } pairs to be compliant with OpenAPI, given the actual jobs could vary between institutions
    jobs: [
      { slug: 'rm', title: 'Relationship Manager' },
      { slug: 'ra', title: 'Research Analyst' },
      { slug: 'co', title: 'Credit Officer' },
      { slug: 'po', title: 'Product Owner' }
    ],
    submissionTypes: [
      { slug: 'new-facility', title: 'New Facility' },
      { slug: 'limit-increase', title: 'Limit Increase' },
      { slug: 'limit-reduction', title: 'Limit Reduction' },
      { slug: 'risk-grading', title: 'Risk Grading' },
      { slug: 'approval-review', title: 'Approval Review' },
      { slug: 'other', title: 'Other' }
    ],
    submissionTemplate: [
      { slug: 'summary', title: 'Summary' },
      { slug: 'other-limits', title: 'Other Limits' },
      { slug: 'notional', title: 'Contingent / Other Liabilities  (not Grouped) / Notional Grouping' },
      { slug: 'decision', title: 'Decision Required' },
      {
        slug: 'facility-uncommitted',
        title: 'Comment on whether the facility is uncommitted and / or unadvised'
      },
      { slug: 'additional-item', title: 'Additional item for approving and noting' },
      { slug: 'transaction-overview', title: 'Transaction overview' },
      { slug: 'pricing-summary', title: 'Pricing Summary' },
      { slug: 'relationship', title: 'Relationship / Credit Strategy / Rationale' },
      { slug: 'terms', title: 'Key Terms & Conditions' },
      { slug: 'business-summary', title: 'Customer, Business and Industry Summary' },
      { slug: 'risk-grading', title: 'Risk Grading' },
      { slug: 'financial-analysis', title: 'Financial Analysis' },
      { slug: 'risks', title: 'Risks & Mitigants' },
      { slug: 'assumptions', title: 'Basis of Financial Analysis & Financial Forecast Assumptions' },
      { slug: 'conclusions', title: 'Conclusions on Financial Analysis - Historical' }
    ],
    submissionStatus: [
      { slug: 'drafting', title: 'Drafting' },
      { slug: 'pending', title: 'Pending formal decision' },
      { slug: 'approved', title: 'Approved' },
      { slug: 'declined', title: 'Declined' },
      { slug: 'withdrawn', title: 'Withdrawn' }
    ],
    reviewStatus: {
      // if a submission's workflow step is 'Credit review' then only allow formal reviews
    // QUESTION: given the new workflow structure, how do we enforce this?
      formal: [
        { slug: 'endorsed', title: 'Endorsed' },
        { slug: 'rework', title: 'Rework required' },
        { slug: 'pending', title: 'Pending' },
        { slug: 'abstained', title: 'Abstained' }
      ],
      // if a submission's workflow step is 'Drafting' then only allow informal reviews
    // QUESTION: given the new workflow structure, how do we enforce this?
      informal: [
        { slug: 'approved', title: 'Approved' },
        { slug: 'rework', title: 'Rework required' },
        { slug: 'pending', title: 'Pending' },
        { slug: 'declined', title: 'Declined' }
      ]
    },
    taskStatus: [
      { slug: 'complete', title: 'Complete' },
      { slug: 'incomplete', title: 'Incomplete' }
    ],
    exceptions: [
      {
        slug: 'group-entities-rating',
        title: 'Rating of group entities without financial statements',
        area: 'pricing-summary',
        description: 'Rating of group entities without financial statements'
      }
    ],
    borrowerDefaults: {
        customerType: [
            // This may not be the full list. Sample data only
            { slug: 'individual', title: 'Individual' },
            { slug: 'sole-trader', title: 'Sole Trader' },
            { slug: 'partnership-regulated', title: 'Partnership - Regulated' },
            { slug: 'partnership-unregulated', title: 'Partnership - Unregulated' }, 
            { slug: 'co-operative', title: 'Co-Operative' },
            { slug: 'association-incorporated', title: 'Association - Incorporated' },
            { slug: 'association-unincorporated', title: 'Association - Unincorporated' },
            { slug: 'trust-regulated', title: 'Trust - Regulated' },
            { slug: 'trust-regulated, title: 'Trust - Unregulated' },
            { slug: 'company-public', title: 'Company - Public' },
            { slug: 'company-private', title: 'Company - Private' },
            { slug: 'government', title: 'Government' }
         ],
         customerGroup: [
            // This may not be the full list. Sample data only
            { slug: 'households', title: 'Households' },
            { slug: 'comm-service-org', title: 'Community service organisations' },
            { slug: 'pvt-non-financial-corp', title: 'Private non-financial corporations: Private non-financial investment funds - Real estate investment trusts (REITs)' }
         ],
         cadLevel: [
            // This may not be the full list. Sample data only
            { slug: 'rm-4', title: 'Relationship Manager - Level 4', limit: number // dollar amount  },
            { slug: 'rm-3', title: 'Relationship Manager - Level 3', limit: number // dollar amount  },
            { slug: 'rm-2', title: 'Relationship Manager - Level 2', limit: number // dollar amount  },
            { slug: 'rm-1', title: 'Relationship Manager - Level 1 Small Business Manager', limit: number // dollar amount  },
            { slug: 'am', title: 'Banking Consultant / Assistant Manager', limit: number // dollar amount  },
            { slug: 'am-cad', title: 'Banking Consultant / Assistant Manager - Excess only CAD', limit: number // dollar amount  }, 
         ],
         cmf: [
            // This may not be the full list. Sample data only
            { slug: 'sample-1', title: 'Sample CMF 1' },
            { slug: 'sample-2', title: 'Sample CMF 2' }
         ],
         businessUnit: [
            // This may not be the full list. Sample data only
            { slug: 'business-banking', title: 'Business Banking' },
            { slug: 'institutional', title: 'Institutional' },
            { slug: 'private-banking', title: 'Private Banking' }
         ],
         anzsic: [
            // This may not be the full list. Sample data only
            { slug: 'nursery-production', title: 'Nursery Production (Under Cover)' },
            { slug: 'ice-cream-manufacturing',  title: 'Ice Cream Manufacturing' },
            { slug: 'metal-product-manufacturing',  title: 'Other Basic Non-Ferrous Metal Product Manufacturing' }
         ],
         individualRoles: [
            // This may not be the full list. Sample data only
            { slug: 'board-member', title: 'Board Member' }, 
            { slug: 'chairman', title: 'Chairman' },
            { slug: 'cao', title: 'Chief Analytics Officer' },
            { slug: 'cco', title: 'Chief Commercial Officer' },
            { slug: 'ccusto', title: 'Chief Customer Officer' },
            { slug: 'cdo', title: 'Chief Data Officer' }
         ]      
       }
  }
}
```

### Future

- `TODO`: add API for editing an institution too.
- `TODO`: what about deleting an institution?
- `TODO`: would admins ever need to list institutions?
- `TODO`: have a more refined view of the various user roles.

---

## SUBMISSIONS

### A list of summaries of my current submissions in progress

`GET /submissions`

```text
GET /submissions  => return 200 OK + list of my current submissions in progress

[
  {
    id: string,
    borrower: {
      id: string, // uuid of the borrower
      name: string // display name
    },
    types: [string], // array of slugs, cross referenced with site.submissionTypes
    purposes: [string], // array of purposes, one for each type
    due: string, // in Zulu time format
    isFlagged: bool, // means flagged for this user, not generally
    status: string, // (drafting, credit review, approved, declined, withdrawn) cross referenced with site.submissionStatus
    createdAt: string, // zulu time date string
    actions: string // eg '2 tasks incomplete' or '1 task overdue', etc
  }
]
```

#### Count them

`GET /submissions/count`

```text
GET /submissions/count => return 200 OK + integer number of my current submissions in progress
```

### A list of summaries of submissions for borrowers I am watching

`GET /submissions/watching`

```text
GET /submissions/watching => return 200 OK + list of summaries of submissions for borrowers I am watching

[
  {
    id: string,
    borrower: {
      id: string, // uuid of the borrower
      name: string // display name
    },
    types: [string], // array of slugs, cross referenced with site.submissionTypes
    purposes: [string], // array of purposes, one for each type
    due: string, // in Zulu time format
    isFlagged: bool, // means flagged for this user, not generally
    status: string, // slug cross referenced with site.submissionStatus
    createdAt: string, // zulu time date string
    actions: string // eg '2 tasks incomplete' or '1 task overdue', etc
  }
]
```

### A specific submission (by its `id`)

`GET /submission/:id`

```text
GET /submission/:id => return 200 OK + the submission with the given id

{
  id: string,
  creator: {
    id: string, // uuid of the user that created this submission
    name: string // their display name
  },
  borrower: {
    id: string, // uuid of the borrower
    name: string // display name
  },
  types: [string], // array of slugs, see site.configuration.submissionTypes
  purposes: [string], // array of purposes, one for each type
  due: string, // in Zulu time format
  createdAt: string, // zulu time format
  updatedAt: string, // zulu time format
  note: string, // arbitrary simple text
  isFlagged: bool, // means flagged for this user, not generally
  workflow: {
    steps: [string], // array of step names
    current: number, // index of the current step
    transitions: [ // only the user-initiated transitions for the current step
      {
        to: number, // step to transition to
        label: string // the text to display in the button.
      }
    ]
  },
  status: string, // slug cross referenced with site.submissionStatus
  team: [
    {
      id: string, // uuid of the user
      name: string,
      position: string, // The user's job
      cadLevel: number // the maximum amount this person can approve.  could be null, 0, or undefined?
    }
  ],
  sections: [
    // slugs come from the site.submissionTemplate
    {
      slug: string,
      content: string, // rich text
      comments: [], // an hierarchy of comments and replies (To be decided)
      updatedBy: { // can be null
        id: string, // uuid of the user who last updated the section
        name: string // display name
      },
      exceptions: [
        {
          slug: match the slugs in institution's exception to get the title (displayed as the code),
          type: string, // oneOf 'existing' or 'new', - new exceptions are ones created by the user, and can be edited.
                                                      - existing submissions are populated from the borrower record and can not be edited.
          mitigation: string // rich text is pre-populated from the borrower if type is 'existing'
        }
      ],
      createdAt: string, // zulu time date string
      updatedAt: string // zulu time date string
    }
  ],
  tasks: [
    {
      id: string (uuid),
      submissionId: string, // foreign key
      category: string, // 'decision' or 'work'
      type: string, // one of ['formal' or 'informal'] if category is decision, else 'work'
      status: string, // depends on category and type (see below)
      description: string, // plain text
      createdAt: string, // zulu time format
      updatedAt: string, // zulu time format
      borrower: {
        id: string, // uuid of the borrower
        name: string // display name
      },
      creator: {
        id: string, // uuid of the user the task was created by (always me in this case)
        name: string // display name
      },
      assignee: {
        id: string, // uuid of the user the task is assigned to
        name: string // display name
      },
      due: string, // in Zulu time format
      isFlagged: bool, // means flagged for this user, not generally
    }
  ]
}
```

### Create a submission

`POST /submission`

```text
POST /submission => create a submission => returns 201 CREATED + { id, ...submissionData as per GET }

{
  borrower: string, // uuid of the borrower
  types: [string], // array of slugs, cross referenced with site.submissionTypes (first type connects the workflow)
  purposes: [string], // array of purposes, one for each type
  due: string, // in Zulu time format
  note: string, // plain text
  team: [string] // array of user ids
}
```

- **Note** The submission's `creator` is defined by the token supplied in the `Authorization` header.

### Update a submission

`PATCH /submission/:id`

```text
PATCH /submission/:id { field: value } => returns 204 No Content and updates just the specific fields in the referenced submission

ref https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6
```

### Transition a submission through its workflow

`PUT /submission/:id/transition/:to`

```text
PUT /submission/:id/transition/:to => transition a submission from its current step to the `to` step and return 204 No 
 Content.
```

### Update a specific submission section

`PATCH /submission/:id` as per a submission update

Pass in the field to update, ie `sections` and an object with a `slug` and content you wish to change.

```text
PATCH /submission/:id {
  sections: [
    {
      slug: string, // the section's slug, aligns with institution's `submissionTemplate`
      content: rich text // can be '' but never null
    }
  ]
} => returns 204 No Content.
```

### Update a specific submission section's exception

`PATCH /submission/:id` as per a submission update

Pass in the field to update, ie `sections` and an object with a `slug` and then `exceptions`, in which you provide an object with the exception's slug, and the `mitigation` with with to change.

```text
PATCH /submission/:id {
  sections: [
    {
      slug: string, // the section's slug, aligns with institution's `submissionTemplate`
      exceptions: [
        {
          slug: string, // a specific slug for the exception, unique within a section.
          mitigation: rich text // or null to delete the exception.
        }
      ]
    }
  ]
} => returns 204 No Content.
```

Note `sections` can not be created or deleted but `exceptions` can be.

- To delete an exception provide `null` as the `mitigation` text.
- To create a new one, use a `slug` that's not been used before in this section.

---

## ACTIVITIES

### A list of activities of the submission

```text
GET /submission/:id/activities
```

URL params

- `startAt` will default to one week ago, and
- `endAt` will default to now.

Colours

You provide a `priority` value of 'success' (green), 'warning' (orange), 'info' (grey), or 'error' (red)

These correspond with [the Material UI standard palette](https://material-ui.com/customization/palette/), colours may change in the UI in some circumstances and so are to be regarded as indicative.  But in general (this is a guideline and non-proscriptive), based on what is happening.

The `message` needs to tell a little one sentence story.

Each API endpoint will have its own logic about how to define the activity's `status` and `priority`.

| activity        | status       | priority                                                                         |
| --------------- | ------------ | --------- |
| `create`        | 'Created',   | 'success' |
| `approve`       | 'Approved'   | 'success' |
| `endorse`       | 'Endorsed'   | 'success' |
| `edit`          | 'Updated'    | 'warning' |
| `status change` | 'Updated'    | 'warning' |
| `comment`       | `null`       | `null`    |
| `withdraw`      | 'Withdrew'   | 'warning' |
| `decline`       | 'Declined'   | 'error'   |
| `delete`        | 'Deleted'    | 'error'   |
| `error`         | 'Error'      | 'error'   |

Activities without a `status` or `priority` will not display a [`Chip`](https://material-ui.com/components/chips/) in the activity feed.

```text
GET /submission/:id/activities?startAt=2020-11-30T11:00:00.000Z&endAt=2021-01-01T00:00:00.000Z => return 200 OK and list of activities of the submission

[
  {
    type: string, // eg 'submission', 'decision', 'work', 'comment', or more specific types such as 'submission-section-comment', etc (to be decided)
    timestamp: string, // zulu-time string
    status: string, // optional: i.e 'Overdue' or 'Formally approved' etc.
    priority: string, // one of 'success', 'warning', 'info', or 'error' as documented above
    message: string, // can include the action eg `Alan replied to Peter's comment in the {action} section (35 comments)`
    action: {
      label: string, // used as link text.
      payload: [
        {
          key: string, // eg submissionId
          value: string // eg the submission's id
        },
        {
          key: string, // eg section
          value: string // eg the specific section name
        }
      ]
    },
    meta: [
      {
        key: string, // eg 'comment'
        value: string // eg 'this is a comment'
      }
      {
        key: string, // eg 'authorId'
        value: string // eg 'abcd-1234-5468-cfed'
      }
    ]
  }
]
```

#### Example activity data

```json
[
  {
    "type": "submission",
    "message": "Jay created this submission",
    "timestamp": "2021-01-21T01:47:25Z"
  },
  {
    "type": "decision",
    "action": {
      "label": "View Formal Decision",
      "payload": [
        { "key": "id", "value": "8fd83bc5-df30-4385-98b6-20090da890fd" }
      ]
    },
    "message": "Jay assigned a formal decision to Bob. {action}",
    "timestamp": "2021-01-21T01:47:47Z"
  },
  {
    "type": "work",
    "action": {
      "label": "View Work Task",
      "payload": [
        { "key": "id", "value": "ba234657-fd4e-568d-2ead-0521c31b88e2" }
      ]
    },
    "status": "Withdrawn",
    "priority": "error",
    "message": "Jay withdrew the work task. {action}",
    "timestamp": "2021-01-21T01:49:39Z"
  },
  {
    "type": "submission-section",
    "action": {
      "label": "Transaction overview",
      "payload": [
        { "key": "submissionId", "value": "ba234657-fd4e-568d-2ead-0521c31b88e2" },
        { "key": "section", "value": "transaction-overview" }
      ]
    },
    "message": "Jay edited the {action} section.",
    "timestamp": "2021-01-22T01:51:39Z"
  },
  {
    "meta": [
      { "key": "comment", "value": "And when Alexander saw the breadth of his domain, he wept, for there were no more worlds to conquer. Benefits of a classical education" },
      { "key": "author", "value": "Jay" }
    ],
    "message": "Jay added a comment to a submission",
    "timestamp": "2021-01-21T11:11:47Z"
  },
  {
    "type": "work",
    "action": {
      "label": "View Work Task",
      "payload": [
        { "key": "id", "value": "ba234657-fd4e-568d-2ead-0521c31b88e2" }
      ]
    },
    "status": "Created",
    "priority": "success",
    "message": "Jay assigned a work task to Rachael. {action}",
    "timestamp": "2021-01-25T10:49:39Z",
    "meta": [
      { "key": "taskId", "value": "ba234657-fd4e-568d-2ead-0521c31b88e2" },
      { "key": "due", "value": "2021-03-01T11:00:00.000Z" },
      { "key": "status", "value": "pending" },
      { "key": "assigneeName", "value": "Rachael Relationship" },
      { "key": "assigneeId", "value": "fd4e-568d-2ead-ba234657-568d" }
    ]
  }
]
```

The front end will construct the message using the data in the action, if the `{action}` key is in the message.

So for example:

- the first `activity`, with just a `type`, `message` and `timestamp` will display the `timestamp` and `message` and nothing else.
- the second `activity`, which has a `type`, `message` with an `action` will display the `timestamp`, and the `message` but with a link to view the decision included in place of the `{action}` placeholder. As it has no `status` or `priority` it will not display a coloured status chip. 
- the third `activity` has an `action` and a `status` and `priority`, so will display the `timestamp`, `message` with `action` link, and a `status` chip with the colour set to 'error' (ie red)

Activities can include associated `meta` data that helps the front-end display type specific information and actions.  So for example the activity above with the message "Jay added a comment to a submission" includes as `meta` data the `comment` and the author of the comment.  The subsequent example, a `pending` `work` task, includes enough meta data such that the UI can check to see that the task is overdue or not, and, if the assignee is the current user, add a 'complete task' button within the activity feed.

---

## COMMENTS

`GET /submission/:id/comments`

```text
GET /submission/:id/comments => return 200 OK and list of comments for submission

[
    {
       id: string (uuid),
       text: string, // comment text
       createdAt: string, // in Zulu time format
       createdBy: {
        id: string (uuid),
        name: string
       }, 
       updatedAt: string // in Zulu time format
    }
]
```

`GET /submission/:id/comment/:cid`

```text
GET /submission/:id/comment/:cid => return 200 OK and returns comment

{
    id: string (uuid),
    text: string, // comment text
    createdAt: string, // in Zulu time format
    createdBy: {
        id: string (uuid),
        name: string
    }, 
    updatedAt: string // in Zulu time format
}

```

`PUT /submission/:id/comment/:cid`

```text
POST /submission/:id/comment/:cid => create a comment => returns 201 CREATED + { id: comment_id }
```
---

## TASKS

### A list of tasks that I created

`GET /tasks`

```text
GET /tasks => return 200 OK and list of tasks I created

[
  {
    id: string (uuid),
    submissionId: string, // foreign key
    category: string, // 'decision' or 'work'
    type: string, // one of ['formal' or 'informal'] if category is decision, else 'work'
    description: string, // some description
    status: string, // depends on category and type (see below)
    due: string, // in Zulu time format
    createdAt: string, // zulu time format
    updatedAt: string, // zulu time format
    borrower: {
      id: string, // uuid of the borrower
      name: string // display name
    },
    creator: {
      id: string, // uuid of the user the task was created by (always me in this case)
      name: string // display name
    },
    assignee: {
      id: string, // uuid of the user the task is assigned to
      name: string // display name
    },
    isFlagged: bool, // means flagged for this user, not generally
  }
]
```

#### Notes on decision vs work tasks

##### Decision task status

- if `formal`
  - `pending`
  - `rework required`
  - `approved`
  - `declined`
  - `withdrawn`

- if `informal`
  - `pending`
  - `rework required`
  - `endorsed`
  - `abstained`
  - `withdrawn`

##### Work task status

- `pending`
- `complete`
- `withdrawn`

##### Overdue tasks

Tasks of any kind will display a status of 'overdue' if their status is not 'approved', 'declined', 'endorsed', 'abstained', 'complete', or 'withdrawn', and the task's `due` date is before `today`.  **Note** This 'virtual' status is not stored in the back-end but computed in the front-end only.

#### Count the tasks I created

`GET /tasks/count`

```text
GET /tasks/count => return 200 OK and the number of tasks I created
```

- `TODO`: does this include `inactive` / `overdue` / `closed` tasks

### A list of tasks that are assigned to me

`GET /tasks/assigned`

```text
GET /tasks/assigned => return 200 OK and list of tasks that are assigned to me

[
  {
    id: string (uuid),
    submissionId: string, // foreign key
    category: string, // 'decision' or 'work'
    type: string, // one of ['formal' or 'informal'] if category is decision, else 'work'
    description: string, // some description
    status: string, // depends on category and type (see below)
    due: string, // in Zulu time format
    createdAt: string, // zulu time format
    updatedAt: string, // zulu time format
    borrower: {
      id: string, // uuid of the borrower
      name: string // display name
    },
    creator: {
      id: string, // uuid of the user the task was created by (always me in this case)
      name: string // display name
    },
    assignee: {
      id: string, // uuid of the user the task is assigned to
      name: string // display name
    },
    isFlagged: bool, // means flagged for this user, not generally
  }
]
```

- `TODO`: does this include `inactive` / `overdue` / `closed` tasks

#### Count the tasks assigned to me

`GET /tasks/assigned/count`

```text
GET /tasks/assigned/count => return 200 OK and the number of tasks assigned to me
```

- `TODO`: does this include `inactive` / `overdue` / `closed` tasks

### A specific task, by its `id`

`GET /task/:id`

```text
GET /task/:id => 200 OK and the task details with isFlagged set according to the user in the token in the auth header

{
  id: string (uuid),
  submissionId: string, // foreign key
  category: string, // 'decision' or 'work'
  type: string, // one of ['formal' or 'informal'] if category is decision, else 'work'
  description: string, // some description
  status: string, // depends on category and type (see below)
  due: string, // in Zulu time format
  createdAt: string, // zulu time format
  updatedAt: string, // zulu time format
  borrower: {
    id: string, // uuid of the borrower
    name: string // display name
  },
  creator: {
    id: string, // uuid of the user the task was created by (always me in this case)
    name: string // display name
  },
  assignee: {
    id: string, // uuid of the user the task is assigned to
    name: string // display name
  },
  isFlagged: bool, // means flagged for this user, not generally
}
```

### Create a task

- **Note** The task's `creator` is defined by the token supplied in the `Authorization` header.
- **Note** the `borrower` information does not need to be sent with the request as it can be obtained from the `submission`.

`POST /task`

```text
POST /task { taskData } => create a task => returns 201 CREATED + { id + data as per GET tasks above }

{
  submissionId: string, // uuid of the submission the task is attached to
  category: string, // 'decision' or 'work'
  due: string, // in Zulu time format
  type: string, // one of ['formal' or 'informal'] if category is decision, else 'work'
  description: string, // some description
  status: string, // depends on category and type (see below)
  assignee: string // uuid of the user the task is assigned to
}
```

### Update a task

`PATCH /task/:id`

The patch can also include a `note` field. note is an optional field when task or decision is marked as 'complete'

```text
PATCH /tasks/:id { field: value } => return 204 No Content and update just the specific fields in the referenced task
```

---

## BORROWERS

### Create a Borrower (admin only)

`POST /borrower`

```text
POST /borrower => return 201 Created + { id } (role must include admin for this to work)

{
  name: 'BHP',
  submissionDefaults: [
    {
        slug: "decision-details-and-rationale",
        text: "The quick brown fox jumps over the lazy dog"
    },
    {
        slug: "submission-approval",
        text: "Come out to the coast, we'll get together, have a few laughs!!"
    }
  ],
  // `TODO` : Refer ANZIC-400 for data types
  customerType: string, // "Tier 1 - Financial"
  marketCap: number, // 100000
  cadLevel: number, // 100
  customerGroup: string, // "FINTECH"
  businessUnit: string, // "BU"
  anzsic: string, // eg "B-0600",
  ccrRiskScore: number, // 68
  securityIndex: string, // "ASD23"
  externalRatingAndOutLook: number, // 8
  lastFullReviewAt: string, // zulu time formatted date,
  lastScheduleReviewAt: string, // zulu time formatted date,
  nextScheduleReviewAt: string, // zulu time formatted date,
  riskSignOff: string, // one of "Y", "N", or ""
  regulatoryRequirements: string, // one of "Y", "N", or ""
  exceptions: [] // `TODO`: details to be discussed
  }
}
```

### A list of Borrowers for my institution

`GET /borrowers`

```text
GET /borrowers => return 200 OK + list of borrowers (we don't need the defaults as that's only for the back end)

[
  {
    id: string, // uuid
    name: string
    submissionCount: number
  }
]
```

### A specific borrower

`GET /borrower/:id`

```text
GET /borrower/:id => return 200 OK + the borrower with the given id

[
  {
    id: string, // uuid
    name: string,
    slug: string // unique - created by server
  }
]
```

### Update a specific borrower

`PATCH /borrower/:id`

```text
PATCH /borrower/:id => returns 204 No Content and updates just the specific fields in the referenced Borrower

{ isWatching: true | false }
```

---

## USERS

### Create a user (admin only)

`POST /user`

- User making the request must have an admin role.
- Created user is assigned to the creating user's institution and given default roles.

```text
POST /user => 201 Created, { id } creates a user and sets any defaults accordingly, and returns the user id.

{
  login: "alice",
  password: "aaaaaa",
  firstName: "Alice",
  middleName: "Carol",
  lastName: "deMaravillas",
  email: "alice@example.tes",
  position: "Relationship Manager",
  profileImage: "https://randomuser.me/api/portraits/women/71.jpg", // string can be an url or a base64 encoded image
  cadLevel: 1500
}
```

### Get summaries of the users I can assign to a team

`GET /users`

```text
GET /users => returns 200 OK and a list of users I can assign to a team.
For now this would be users that are employees of the institution but in the future that may change.

[
  {
    id: string, // uuid
    name: string,
    position: string, // the user's job
    cadLevel: number // the maximum amount this person can approve.  could be null, 0, or undefined?
  }
]
```

---

## NOTIFICATIONS

### A list of my notifications

`GET /notifications`

```text
GET /notifications => list of my notifications

[
  {
    id: string, // uuid
    createdAt: string, // zulu time formatted
    text: string, // plain text
    cta: {
      text: string, // eg 'View submission decisions'
      to: string, // eg '/submissions/:id/decisions'
    }
  }
]
```

---

## NEWS

### A list of news items

`GET /news`

```text
GET /news => a list of relevant news items to display on the dashboard

[
  {
    heading: string,
    content: string, // likely HTML formatted, or an image?
    cta: { // cta means 'call to action' - ie what happens when the link or button is clicked. might be null.
      text: string, // eg 'do you want to know more?'
      href: string // 'http://somenews.or.blog/slug'
    }
  }
]
```

---

## WORKFLOWS

### Create a workflow (admin only)

`POST /workflow`

```text
POST /workflow => create a new workflow for the user's institution, and return 201 Created + { created workflow data ala GET (below) }

{
  name: string, // name + version must be unique but the same name can have multiple versions
  version: number,
  initialStatus: string, // 'drafting'
  submissionTypes [string], // array of slugs
  steps: [
    { 
      index: number, // eg 1
      name: string, // eg Drafting
      layout: {
        commentsVisible: bool,
        submissionAnalysisEditable: bool,
      },
      transitions: [
        {
          to: number, // Step number to transition to
          trigger: string, // string indicating type of transition (user-initiated, rework)
          label: string, // label of the transition button
          submissionStatus: string, // Status to change the submission to when performing this transition 
          borrowerRefresh: bool // Do we refresh the borrower profile when performing this transition
        }
      ]
    }
  ]
}
​```

**Note**: The `author` is determined from the user's token

### Get a specific workflow

`GET /workflow/:id`

```text
GET /workflow/:id => 200 Ok and the workflow with the specified id

{
  id: string, // uuid
  name: string, // workflow name (unique in combination with version)
  version: number,
  author: {
    id,
    name
  },
  initialStatus: string, // 'drafting'
  submissionTypes [string], // array of slugs
  steps: [
    {
      index: int, // eg 1
      name: string,   // eg Drafting
      layout: {
        commentsVisible: bool,
        submissionAnalysisEditable: bool,
      },
      transitions: [
        {
          to: number, // Step number to transition to
          trigger: string, // string indicating type of transition (user-initiated, rework)
          label: string, // label of the transition button
          submissionStatus: string, // Status to change the submission to when performing this transition 
          borrowerRefresh: bool // Do we refresh the borrower profile when performing this transition
        }
      ]
    }
  ],
  publishedAt: string, // optional zulu-time string - if not present then it's a draft workflow
  createdAt: string, // in Zulu time format
  updatedAt: string // in Zulu time format
}
```

### A list of knock-on workflow changes should this workflow be published

`GET /workflows/:id/publishApproval`

```text
GET /workflows/:id/publishApproval => return 200 OK and a list of workflow changes if this were to be published

[
 { original, updated } // original and updated are the affected workflows.
]
```

### Update a workflow

You can't edit a published workflow.

#### Publish a workflow

`PATCH /workflow/:id/publish`

```text
PATCH /workflow/:id/publish => publish an unpublished workflow, and make any associated changes
```

See `GET /workflows/:id/publishApproval` above.

#### Update an unpublished workflow

**Note**: You should not allow this to change the `publishedAt` field.

`PATCH /workflow/:id`

```text
PATCH /workflow/:id { field, value } => return 204 No Content and update an unpublished workflow
```

### Delete an unpublished workflow

`DELETE /workflow/:id`

```text
DELETE /workflow/:id  => return 202 Deleted and delete an unpublished workflow
```

### A list of the most recent versions of any workflow

`GET /workflows`

```text
GET /workflows => get an array of most recent workflows

[
  {
    id: string, // uuid
    name: string, // workflow name (unique in combination with version)
    version: number,
    author: {
      id,
      name
    },
    initialStatus: string, // 'drafting'
    submissionTypes [string], // array of slugs
    steps: [
      {
        index: int, // eg 1
        name: string,   // eg Drafting
        layout: {
          commentsVisible: bool,
          submissionAnalysisEditable: bool,
        },
        transitions: [
          {
            to: number, // Step number to transition to
            trigger: string, // string indicating type of transition (user-initiated, rework)
            label: string, // label of the transition button
            submissionStatus: string, // Status to change the submission to when performing this transition 
            borrowerRefresh: bool // Do we refresh the borrower profile when performing this transition
          }
        ]
      }
    ],
    publishedAt: string, // optional zulu-time string - if not present then it's a draft workflow
    createdAt: string, // in Zulu time format
    updatedAt: string // in Zulu time format
  }
​]
```

**Question**: Do I need to query for `published` vs `unpublished`?

---

## Links and other notes

- [`itnext.io/whats-a-slug`](https://itnext.io/whats-a-slug-f7e74b6c23e0).
- [RFC 7617: Basic Authentication](https://tools.ietf.org/html/rfc7617).
