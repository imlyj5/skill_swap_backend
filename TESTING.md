# Integration Testing Guide

This guide explains how to run integration tests for the SkillSwap backend application.

## Test Structure

The integration tests are located in `src/test/java/com/example/SkillSwap/controller/` and include:

- `ProfileControllerIntegrationTest.java` - Tests user CRUD operations
- `SkillControllerIntegrationTest.java` - Tests skill CRUD operations  
- `AuthControllerIntegrationTest.java` - Tests authentication (login/register)

## Test Configuration

Tests use:
- **H2 in-memory database** for fast, isolated testing
- **Test profile** (`application-test.properties`) with specific test settings
- **MockMvc** for HTTP request/response testing
- **Automatic cleanup** between tests

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=ProfileControllerIntegrationTest
mvn test -Dtest=SkillControllerIntegrationTest
mvn test -Dtest=AuthControllerIntegrationTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=ProfileControllerIntegrationTest#testCreateUser
```

### Run Tests with Detailed Output
```bash
mvn test -Dspring.profiles.active=test
```

## Test Coverage

### ProfileController Tests
- ✅ Create new user
- ✅ Get all users
- ✅ Get user by ID
- ✅ Update user
- ✅ Delete user
- ✅ Handle user not found

### SkillController Tests
- ✅ Create new skill
- ✅ Get all skills
- ✅ Get skill by ID
- ✅ Handle skill not found

### AuthController Tests
- ✅ Successful login
- ✅ Failed login (wrong credentials)
- ✅ Successful registration
- ✅ Registration with duplicate email

## Test Data

Tests create and clean up their own data:
- Each test method is independent
- Database is cleared before each test (`@BeforeEach`)
- No test data persists between tests

## Debugging Tests

### View SQL Queries
Tests show SQL queries in console (configured in `application-test.properties`)

### H2 Console (Optional)
If needed, you can access the H2 console during tests at:
```
http://localhost:8080/h2-console
```
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

### Common Issues

1. **Test fails with "User not found"**
   - Check if the test creates the user before testing
   - Verify the user ID is correct

2. **Database connection issues**
   - Ensure H2 dependency is in `pom.xml`
   - Check `application-test.properties` configuration

3. **JSON parsing errors**
   - Verify request body format matches expected structure
   - Check if all required fields are provided

## Adding New Tests

To add new integration tests:

1. Create test class in `src/test/java/com/example/SkillSwap/controller/`
2. Use `@SpringBootTest` and `@ActiveProfiles("test")`
3. Inject required repositories and MockMvc
4. Add `@BeforeEach` to clean up data
5. Write test methods with descriptive names

Example:
```java
@Test
void testYourNewFeature() throws Exception {
    // Setup test data
    // Make HTTP request
    // Verify response
}
``` 