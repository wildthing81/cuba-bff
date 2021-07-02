# credible-be

 APIs built on the Cuba Platform. 

- Main language - Kotlin, Spring5
- Testing - JUnit5, Mockk, Strikt(Assertions), TestContainers
- ORM - JPQL(Eclipselink)
- Build - Gradle
- Database - Postgres

See [API Documentation](/API.md)

## To build

Various gradle tasks created by CUBA framework are available in project for building & testing whole project or
individual modules _**(core,global,web)_**

## Development

Under development: features outlined below may not be final.

<!-- prettier-ignore -->
| branch  | ci status | codacy code coverage | codacy code quality | notes |
| ------  | --------- | -------- | ------------------- | ----- |
| develop | [![CircleCI](https://circleci.com/gh/ANZi-Credible/credible-be/tree/develop.svg?style=svg&circle-token=10014f7cd71881bdc6fd9d1fd0f6fb8bba974f0a)](https://circleci.com/gh/ANZi-Credible/credible-be/tree/develop) | [![Codacy Badge](https://app.codacy.com/project/badge/Coverage/e2b6bc19f0b74b28a762dd9aa3debefe)](https://www.codacy.com?utm_source=github.com&utm_medium=referral&utm_content=ANZi-Credible/credible-be&utm_campaign=Badge_Coverage) | [![Codacy Badge](https://app.codacy.com/project/badge/Grade/e2b6bc19f0b74b28a762dd9aa3debefe)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ANZi-Credible/credible-be&amp;utm_campaign=Badge_Grade) | Work in progress |
| main    | [![CircleCI](https://circleci.com/gh/ANZi-Credible/credible-be/tree/main.svg?style=svg&circle-token=10014f7cd71881bdc6fd9d1fd0f6fb8bba974f0a)](https://circleci.com/gh/ANZi-Credible/credible-be/tree/main) | N/A | N/A | Latest release |

### Git strategy

- Currently, doing trunk based development on forks since team is small.
- Will move to Git flow (feature branches) when needed.

However, every commit message must:

- **start with JIRA ticket no.**
- **tell a concise & clear functional story**
- **use imperative style** e.g. Add tests for 'X' or Fix 'Y' or Create 'Z'

### Deploy code + database for local development

1. Install Docker & CUBA plugin for IntelliJ(IntelliJ 2020.2.3 provides stable support )
2. Install Docker for desktop(Mac or Windows) & Connect it within IntelliJ
3. Edit the docker-compose.yml configuration
    - add 'clean buildWar' gradle tasks of project 
    - :white_check_mark: --build, force build images
    
   Run `<project>/docker-compose.yml` by clicking on green gutter-icon on 'services' key in file. 
   
4. Open IntelliJ 'Services' view to find 2 containers - tomcat & postgres running
5. the docker-compose.yml runs tomcat in debug mode which allows hot reload

### Coding Style

- Import the committed `codestyle.xml` into IntelliJ Preferences -> CodeStyle -> Kotlin scheme to set 
  coding standards
- Install `Detekt` plugin & enable its settings(`Enable Detekt`, `Enable formatting rules`, 
  `Treat detekt findings as errors` etc ) in Preferences -> Tools -> detekt 
- Add KDoc to significant functions in Services etc. Install KDoc-generator plugin for that.
- use explanatory variable names

### Import order in Kotlin classes

Define following sequence of imports in IntelliJ settings

```java
// static all other,
java.*, 
javax.*,
kotlin.*,
org.*, 
com.*
 
// all other imports
```

## Deployment

CircleCI config.yml in _.circleci_ folder runs CI/CD pipe-line of following jobs
- code-coverage, 
- build and register ECR image,
- deploy image to ECS cluster

for **_dev,staging_** environments


## Credible config
 config parameters are to be stored in cuba sys config database table. For now, ck editor parameters
 are to be stored through the backend. Backend is to expose an api to update/insert config data, and seeder project
  is to be updated.

```
credible.ckeditor.environmentId	= environmentidvalue
credible.ckeditor.secret = secretvalue
```
## Health check url
health check endpoints for **_Web,Core_** blocks are available at
[/app-core/rest/health] (http://localhost:8080/app-core/remoting/health)
[/app/rest/health] (http://localhost:8080/app/rest/health)

=> returns ok


## Api testing through postman
Postman collection is part of this project and can be found under
/postman directory. Import these environment files and collection to test various apis. 
=> returns ok

## Swagger url

Swagger definitions are available at 
[**host**/app/rest/swagger-docs] 
[**host**/app/rest/swagger-ui.html]

