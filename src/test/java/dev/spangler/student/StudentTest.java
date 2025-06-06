package dev.spangler.student;

import dev.spangler.cache.RedisService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentTest {

    private static Long studentId;

    @Inject
    RedisService redisService;

    @Test
    @Order(1)
    void testCreateStudent() {
        StudentDto student = new StudentDto("Test User", "testuser@email.com", "9876543210");

        Number id = given()
                .contentType(ContentType.JSON)
                .body(student)
                .when()
                .post("/api/v1/students")
                .then()
                .statusCode(201)
                .body("name", Matchers.equalTo("Test User"))
                .body("email", Matchers.equalTo("testuser@email.com"))
                .body("mobile", Matchers.equalTo("9876543210"))
                .extract().path("id");

        studentId = id.longValue();
    }

    @Test
    @Order(2)
    void testGetStudentById() {
        given()
                .pathParam("id", studentId)
                .when()
                .get("/api/v1/students/{id}")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(studentId.intValue()));
    }

    @Test
    @Order(3)
    void testGetAllStudents() {
        given()
                .when()
                .get("/api/v1/students?page=0&size=10")
                .then()
                .statusCode(200)
                .body("size()", Matchers.greaterThanOrEqualTo(1));

        // Check if cache exists
        String key = "students:page:0:size:10";
        String cached = redisService.getValue(key);
        Assertions.assertNotNull(cached, "Expected cache to be populated after GET");
    }


    @Test
    @Order(4)
    void testUpdateStudent() {
        StudentDto updated = new StudentDto("Updated User", "updated@email.com", "9876543210");

        given()
                .pathParam("id", studentId)
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .put("/api/v1/students/{id}")
                .then()
                .statusCode(200)
                .body("name", Matchers.equalTo("Updated User"))
                .body("email", Matchers.equalTo("updated@email.com"));
    }

    @Test
    @Order(5)
    void testDeleteStudent() {
        given()
                .pathParam("id", studentId)
                .when()
                .delete("/api/v1/students/{id}")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(6)
    void testGetDeletedStudent() {
        given()
                .pathParam("id", studentId)
                .when()
                .get("/api/v1/students/{id}")
                .then()
                .statusCode(404);
    }
}
