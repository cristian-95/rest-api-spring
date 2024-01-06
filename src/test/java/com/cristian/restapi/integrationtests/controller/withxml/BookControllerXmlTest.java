package com.cristian.restapi.integrationtests.controller.withxml;

import com.cristian.restapi.configs.TestConfigs;
import com.cristian.restapi.integrationtests.testcontainers.AbstractIntegrationTest;
import com.cristian.restapi.integrationtests.vo.BookVO;
import com.cristian.restapi.integrationtests.vo.security.AccountCredentialVO;
import com.cristian.restapi.integrationtests.vo.security.TokenVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookControllerXmlTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static XmlMapper objectMapper;

    private static BookVO book;

    @BeforeAll
    public static void setup() {
        objectMapper = new XmlMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        book = new BookVO();
    }


    @Test
    @Order(0)
    public void testAuthorization() throws JsonProcessingException {
        AccountCredentialVO user = new AccountCredentialVO("leandro", "admin123");

        var accessToken =
                given()
                        .basePath("/auth/signin")
                        .port(TestConfigs.SERVER_PORT)
                        .contentType(TestConfigs.CONTENT_TYPE_XML)
                        .accept(TestConfigs.CONTENT_TYPE_XML)
                        .body(user)
                        .when()
                        .post()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(TokenVO.class)
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

        var content =
                given()
                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_XML)
                        .accept(TestConfigs.CONTENT_TYPE_XML)
                        .body(book)
                        .when()
                        .post()
                        .then()
                        .statusCode(201)
                        .extract()
                        .body()
                        .asString();
        book = objectMapper.readValue(content, BookVO.class);


        assertNotNull(book);
        assertNotNull(book.getId());
        assertNotNull(book.getTitle());
        assertNotNull(book.getAuthor());

        assertNotNull(book.getPrice());
        assertTrue(book.getId() > 0);
        assertEquals("Crime e Castigo", book.getTitle());
        assertEquals("Fiódor Dostoiévski", book.getAuthor());
        assertEquals(49.99, book.getPrice());
    }

    @Test
    @Order(2)
    public void testUpdate() throws JsonProcessingException {

        book.setTitle("Crime e Castigo 2 - A vingança");

        var content =
                given()
                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_XML)
                        .accept(TestConfigs.CONTENT_TYPE_XML)
                        .body(book)
                        .when()
                        .put()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();
        BookVO persistedBook = objectMapper.readValue(content, BookVO.class);


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

        var content =
                given()
                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_XML)
                        .accept(TestConfigs.CONTENT_TYPE_XML)
                        .header(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_ERUDIO)
                        .pathParams("id", book.getId())
                        .when()
                        .get("{id} ")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();
        BookVO foundBook = objectMapper.readValue(content, BookVO.class);
        book = foundBook;

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
                .spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .pathParam("id", book.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(5)
    public void testFindAll() throws JsonProcessingException {

        var contentString =
                given()
                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_XML)
                        .accept(TestConfigs.CONTENT_TYPE_XML)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();

        List<BookVO> books = objectMapper.readValue(contentString, new TypeReference<List<BookVO>>() {
        });

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
                .spec(specificationWithoutToken)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
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