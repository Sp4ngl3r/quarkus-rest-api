package dev.spangler.student.cache;

import dev.spangler.cache.RedisService;
import dev.spangler.student.Student;
import dev.spangler.student.StudentDto;
import dev.spangler.student.StudentResponseDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.vertx.core.json.Json;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StudentCacheTest {

    @Inject
    RedisService redisService;

    private static Long studentId;
    private static final String INDIVIDUAL_PREFIX = "student:";
    private static final String PAGINATED_PREFIX = "students:page:0:size:10";

    @Test
    @Order(1)
    void testCreateStudentAndCheckCache() {
        StudentDto studentDto = new StudentDto("Cached User", "cache@email.com", "9999999999");

        Number id = given()
                .contentType(ContentType.JSON)
                .body(studentDto)
                .when()
                .post("/api/v1/students")
                .then()
                .statusCode(201)
                .body("name", Matchers.equalTo("Cached User"))
                .extract().path("id");

        studentId = id.longValue();

        // Check if individual student is cached
        String cacheKey = INDIVIDUAL_PREFIX + studentId;
        String cached = redisService.getValue(cacheKey);
        assertNotNull(cached);
        Student cachedStudent = Json.decodeValue(cached, Student.class);
        assertEquals("Cached User", cachedStudent.name);

        // Check paginated cache invalidation
        assertNull(redisService.getValue(PAGINATED_PREFIX));
    }

    @Test
    @Order(2)
    void testFetchPaginatedStudentsAndCacheIt() {
        // First request populates the cache
        List<StudentResponseDto> result =
                given()
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .when()
                        .get("/api/v1/students")
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath().getList(".", StudentResponseDto.class);

        assertFalse(result.isEmpty());

        // Paginated result should now be cached
        String cachedListJson = redisService.getValue(PAGINATED_PREFIX);
        assertNotNull(cachedListJson);
        List<StudentResponseDto> cachedList = redisService.getListValue(PAGINATED_PREFIX, StudentResponseDto.class);
        assertNotNull(cachedList);
        assertFalse(cachedList.isEmpty());
    }

    @Test
    @Order(3)
    void testUpdateStudentAndInvalidateCaches() {
        StudentDto updated = new StudentDto("Updated Cache User", "updated@email.com", "8888888888");

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", studentId)
                .body(updated)
                .when()
                .put("/api/v1/students/{id}")
                .then()
                .statusCode(200)
                .body("name", Matchers.equalTo("Updated Cache User"));

        // Verify individual cache updated
        String updatedCache = redisService.getValue(INDIVIDUAL_PREFIX + studentId);
        assertNotNull(updatedCache);
        Student updatedStudent = Json.decodeValue(updatedCache, Student.class);
        assertEquals("Updated Cache User", updatedStudent.name);

        // Verify paginated cache invalidated
        assertNull(redisService.getValue(PAGINATED_PREFIX));
    }

    @Test
    @Order(4)
    void testDeleteStudentAndClearCache() {
        given()
                .pathParam("id", studentId)
                .when()
                .delete("/api/v1/students/{id}")
                .then()
                .statusCode(200);

        // Individual cache should be gone
        assertNull(redisService.getValue(INDIVIDUAL_PREFIX + studentId));

        // Paginated cache should be invalidated again
        assertNull(redisService.getValue(PAGINATED_PREFIX));
    }

    @Test
    @Order(5)
    void testGetDeletedStudentReturns404() {
        given()
                .pathParam("id", studentId)
                .when()
                .get("/api/v1/students/{id}")
                .then()
                .statusCode(404);
    }
}
