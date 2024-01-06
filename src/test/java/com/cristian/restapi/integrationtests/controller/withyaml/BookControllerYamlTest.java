package com.cristian.restapi.integrationtests.controller.withyaml;

import com.cristian.restapi.configs.TestConfigs;
import com.cristian.restapi.integrationtests.controller.withyaml.mapper.YMLMapper;
import com.cristian.restapi.integrationtests.testcontainers.AbstractIntegrationTest;
import com.cristian.restapi.integrationtests.vo.BookVO;
import com.cristian.restapi.integrationtests.vo.security.AccountCredentialVO;
import com.cristian.restapi.integrationtests.vo.security.TokenVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookControllerYamlTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static YMLMapper ymlMapper;

    private static BookVO book;

    @BeforeAll
    public static void setup() {
        ymlMapper = new YMLMapper();

        book = new BookVO();
    }


    @Test
    @Order(0)
    public void testAuthorization() throws JsonProcessingException {
        AccountCredentialVO user = new AccountCredentialVO("leandro", "admin123");

        var accessToken =
                given()
                        .config(
                                RestAssuredConfig
                                        .config()
                                        .encoderConfig(
                                                EncoderConfig.
                                                        encoderConfig()
                                                        .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))

                        .basePath("/auth/signin")
                        .port(TestConfigs.SERVER_PORT)
                        .contentType(TestConfigs.CONTENT_TYPE_YML)
                        .accept(TestConfigs.CONTENT_TYPE_YML)
                        .body(user, ymlMapper)
                        .when()
                        .post()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(TokenVO.class, ymlMapper)
                        .getAccessToken();

        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken)
                .setBasePath("/api/books/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
    }

    @Test
    @Order(1)
    public void testCreate() throws JsonProcessingException {
        mockBook();

        var persistedBook =
                given()
                        .config(
                                RestAssuredConfig
                                        .config()
                                        .encoderConfig(
                                                EncoderConfig.
                                                        encoderConfig()
                                                        .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))

                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_YML)
                        .accept(TestConfigs.CONTENT_TYPE_YML)
                        .body(book, ymlMapper)
                        .when()
                        .post()
                        .then()
                        .statusCode(201)
                        .extract()
                        .body()
                        .as(BookVO.class, ymlMapper);
        book = persistedBook;


        assertNotNull(persistedBook);
        assertNotNull(persistedBook.getId());
        assertNotNull(persistedBook.getTitle());
        assertNotNull(persistedBook.getAuthor());

        assertNotNull(persistedBook.getPrice());
        assertTrue(persistedBook.getId() > 0);
        assertEquals("Crime e Castigo", persistedBook.getTitle());
        assertEquals("Fiódor Dostoiévski", persistedBook.getAuthor());
        assertEquals(49.99, persistedBook.getPrice());
    }

    @Test
    @Order(2)
    public void testUpdate() throws JsonProcessingException {

        book.setTitle("Crime e Castigo 2 - A vingança");

        var persistedBook =
                given()
                        .config(
                                RestAssuredConfig
                                        .config()
                                        .encoderConfig(
                                                EncoderConfig.
                                                        encoderConfig()
                                                        .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))

                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_YML)
                        .accept(TestConfigs.CONTENT_TYPE_YML)
                        .body(book, ymlMapper)
                        .when()
                        .put()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(BookVO.class, ymlMapper);


        assertNotNull(persistedBook);
        assertNotNull(persistedBook.getId());
        assertNotNull(persistedBook.getTitle());
        assertNotNull(persistedBook.getAuthor());
        assertNotNull(persistedBook.getPrice());
        assertEquals(book.getId(), persistedBook.getId());
        assertEquals("Crime e Castigo 2 - A vingança", persistedBook.getTitle());
        assertEquals("Fiódor Dostoiévski", persistedBook.getAuthor());
        assertEquals(49.99, persistedBook.getPrice());
    }


    @Test
    @Order(3)
    public void testFindById() throws JsonProcessingException {

        var foundBook =
                given()
                        .config(
                                RestAssuredConfig
                                        .config()
                                        .encoderConfig(
                                                EncoderConfig.
                                                        encoderConfig()
                                                        .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))

                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_YML)
                        .accept(TestConfigs.CONTENT_TYPE_YML)
                        .header(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_ERUDIO)
                        .pathParams("id", book.getId())
                        .when()
                        .get("{id} ")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(BookVO.class, ymlMapper);

        assertNotNull(foundBook);
        assertNotNull(foundBook.getId());
        assertNotNull(foundBook.getTitle());
        assertNotNull(foundBook.getAuthor());
        assertNotNull(foundBook.getPrice());
        assertTrue(foundBook.getId() > 0);
        assertEquals("Crime e Castigo 2 - A vingança", foundBook.getTitle());
        assertEquals("Fiódor Dostoiévski", foundBook.getAuthor());
        assertEquals(49.99, foundBook.getPrice());
    }

    @Test
    @Order(4)
    public void testDelete() throws JsonProcessingException {
        given()
                .config(
                        RestAssuredConfig
                                .config()
                                .encoderConfig(
                                        EncoderConfig.
                                                encoderConfig()
                                                .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))

                .spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .pathParam("id", book.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(5)
    public void testFindAll() throws JsonProcessingException {

        var content =
                given()
                        .config(
                                RestAssuredConfig
                                        .config()
                                        .encoderConfig(
                                                EncoderConfig.
                                                        encoderConfig()
                                                        .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))

                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_YML)
                        .accept(TestConfigs.CONTENT_TYPE_YML)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(BookVO[].class, ymlMapper);

        List<BookVO> books = Arrays.asList(content);

        var foundBookOne = books.getFirst();
        assertNotNull(foundBookOne.getId());
        assertNotNull(foundBookOne.getTitle());
        assertNotNull(foundBookOne.getAuthor());
        assertNotNull(foundBookOne.getPrice());
        assertEquals(1, foundBookOne.getId());
        assertEquals("Working effectively with legacy code", foundBookOne.getTitle());
        assertEquals("Michael C. Feathers", foundBookOne.getAuthor());
        assertEquals(49.00, foundBookOne.getPrice());


        var foundBookFive = books.get(4);
        assertNotNull(foundBookFive.getId());
        assertNotNull(foundBookFive.getTitle());
        assertNotNull(foundBookFive.getAuthor());
        assertNotNull(foundBookFive.getPrice());
        assertEquals(5, foundBookFive.getId());
        assertEquals("Code complete", foundBookFive.getTitle());
        assertEquals("Steve McConnell", foundBookFive.getAuthor());
        assertEquals(58.00, foundBookFive.getPrice());

        var foundBookTen = books.get(9);
        assertNotNull(foundBookTen.getId());
        assertNotNull(foundBookTen.getTitle());
        assertNotNull(foundBookTen.getAuthor());
        assertNotNull(foundBookTen.getPrice());
        assertEquals(10, foundBookTen.getId());
        assertEquals("O poder dos quietos", foundBookTen.getTitle());
        assertEquals("Susan Cain", foundBookTen.getAuthor());

        assertEquals(123.00, foundBookTen.getPrice());
    }

    @Test
    @Order(6)
    public void testFindAllWithoutToken() throws JsonProcessingException {

        var specificationWithoutToken = new RequestSpecBuilder()
                .setBasePath("/api/books/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        given()
                .config(
                        RestAssuredConfig
                                .config()
                                .encoderConfig(
                                        EncoderConfig.
                                                encoderConfig()
                                                .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))

                .spec(specificationWithoutToken)
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .when()
                .get()
                .then()
                .statusCode(403);
    }

    private void mockBook() {
        book.setTitle("Crime e Castigo");
        book.setAuthor("Fiódor Dostoiévski");
        book.setLaunchDate(new Date());
        book.setPrice(49.99);
    }

}