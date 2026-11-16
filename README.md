# Intelligent secure HR management backend

A REST API built with Spring Boot around 2 JPA entities, using PostgreSQL.

## Tech stack

- Java 25, built with Maven
- Spring Boot 3.5.7
- Spring Web (REST controllers)
- Spring Data JPA (Hibernate)
- Spring Data REST
- Spring Security
- OAuth2 resource server
- Bean Validation
- Spring Boot Actuator
- Lombok
- PostgreSQL driver
- H2 database
- JJWT (JSON Web Tokens)
- Database: PostgreSQL (`hr_managee_db` in `application.properties`)

## Data model

| Entity | Fields |
|---|---|
| `User` | — |
| `Employee` | — |

## REST endpoints

Found in the controllers (`OAuth2Controller`, `HRDocumentController`):

| Method | Path |
|---|---|
| GET | `/api/oauth2/providers` |
| GET | `/api/oauth2/info` |
| POST | `/api/documents` |
| GET | `/api/documents` |
| GET | `/api/documents/{id}` |
| GET | `/api/documents/{id}/download` |
| GET | `/api/documents/employee/{employeeId}` |
| DELETE | `/api/documents/{id}` |

## Project structure

- `tn.sesame.rh_management_backend`: RhManagementBackendApplication
- `tn.sesame.rh_management_backend.Entities`: EmbeddingModel, Employee, HRDocument, User
- `tn.sesame.rh_management_backend.Enumerations`: ContractType, DocumentAccessLevel, EmployeeDepartment, EmployeeJobTitle, UserRole
- `tn.sesame.rh_management_backend.Repositories`: EmbeddingModelRepository, EmployeeRepository, HRDocumentRepository, UserRepository
- `tn.sesame.rh_management_backend.Services`: AuthenticationService, CustomUserDetailsService, EmployeeService, HRDocumentService, UserService
- `tn.sesame.rh_management_backend.Validations`: SalaryRangeValidator, ValidSalaryRange, myAnnotation
- `tn.sesame.rh_management_backend.Web`: AuthController, EmployeeController, HRDocumentController, OAuth2Controller, UserController
- `tn.sesame.rh_management_backend.configurations`: JwtAuthenticationFilter, JwtUtil, OAuth2FailureHandler, OAuth2ResourceServerConfig, OAuth2SuccessHandler, SecurityConfig
- `tn.sesame.rh_management_backend.dto`: AddEmployeeDto, AuthResponse, ChangePasswordRequest, EmployeeCreateRequest, EmployeeDTO, EmployeeDto, EmployeeUpdateRequest, HRDocumentDTO, HRDocumentUploadRequest, LoginDto, LoginRequest, RefreshTokenRequest …
- `tn.sesame.rh_management_backend.exceptions`: ApiError, BadRequestException, ConflictException, ForbiddenException, GlobalExceptionHandler, NotFoundException, UnauthorizedException
- Tests: 1 test class

## How to run

Requirements: a JDK 25+ and Maven (or the included Maven wrapper).
Create the `hr_managee_db` database and set your own username and password in `src/main/resources/application.properties`.

```bash
./mvnw spring-boot:run
```

The API then listens on `http://localhost:9995`.

---

Author: [Chaouki Bayoudhi](https://github.com/ChaoukiBayoudhi)
