package com.cristian.restapi.integrationtests.controller.withjson;

import com.cristian.restapi.configs.TestConfigs;
import com.cristian.restapi.integrationtests.testcontainers.AbstractIntegrationTest;
import com.cristian.restapi.integrationtests.vo.PersonVO;
import com.cristian.restapi.integrationtests.vo.security.AccountCredentialVO;
import com.cristian.restapi.integrationtests.vo.security.TokenVO;
import com.cristian.restapi.integrationtests.vo.wrappers.WrapperPersonVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonControllerJsonTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static ObjectMapper objectMapper;

    private static PersonVO person;

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        person = new PersonVO();
    }


    @Test
    @Order(0)
    public void testAuthorization() throws JsonProcessingException {
        AccountCredentialVO user = new AccountCredentialVO("leandro", "admin123");

        var accessToken =
                given()
                        .basePath("/auth/signin")
                        .port(TestConfigs.SERVER_PORT)
                        .contentType(TestConfigs.CONTENT_TYPE_JSON)
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
                .setBasePath("/api/people/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
    }

    @Test
    @Order(1)
    public void testCreate() throws JsonProcessingException {
        mockPerson();

        var content =
                given()
                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_JSON)
                        .body(person)
                        .when()
                        .post()
                        .then()
                        .statusCode(201)
                        .extract()
                        .body()
                        .asString();
        PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
        person = persistedPerson;

        assertNotNull(persistedPerson);
        assertNotNull(persistedPerson.getId());
        assertNotNull(persistedPerson.getFirstName());
        assertNotNull(persistedPerson.getLastName());
        assertNotNull(persistedPerson.getAddress());
        assertNotNull(persistedPerson.getGender());
        assertTrue(persistedPerson.getEnabled());
        assertTrue(persistedPerson.getId() > 0);
        assertEquals("Rodion", persistedPerson.getFirstName());
        assertEquals("Raskólnikov", persistedPerson.getLastName());
        assertEquals("São Petesburgo - Russia", persistedPerson.getAddress());
        assertEquals("Male", persistedPerson.getGender());
    }

    @Test
    @Order(2)
    public void testUpdate() throws JsonProcessingException {

        person.setFirstName("Ródia");

        var content =
                given()
                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_JSON)
                        .body(person)
                        .when()
                        .put()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();
        PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
        person = persistedPerson;

        assertNotNull(persistedPerson);
        assertNotNull(persistedPerson.getId());
        assertNotNull(persistedPerson.getFirstName());
        assertNotNull(persistedPerson.getLastName());
        assertNotNull(persistedPerson.getAddress());
        assertNotNull(persistedPerson.getGender());
        assertTrue(persistedPerson.getEnabled());

        assertEquals(person.getId(), persistedPerson.getId());
        assertEquals("Ródia", persistedPerson.getFirstName());
        assertEquals("Raskólnikov", persistedPerson.getLastName());
        assertEquals("São Petesburgo - Russia", persistedPerson.getAddress());
        assertEquals("Male", persistedPerson.getGender());
    }


    @Test
    @Order(3)
    public void testDisablePersonById() throws JsonProcessingException {

        var content =
                given()
                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_JSON)
                        .pathParams("id", person.getId())
                        .when()
                        .patch("{id} ")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();
        PersonVO foundedPerson = objectMapper.readValue(content, PersonVO.class);
        person = foundedPerson;

        assertNotNull(foundedPerson);
        assertNotNull(foundedPerson.getId());
        assertNotNull(foundedPerson.getFirstName());
        assertNotNull(foundedPerson.getLastName());
        assertNotNull(foundedPerson.getAddress());
        assertNotNull(foundedPerson.getGender());

        assertFalse(foundedPerson.getEnabled());


        assertTrue(foundedPerson.getId() > 0);
        assertEquals("Ródia", foundedPerson.getFirstName());
        assertEquals("Raskólnikov", foundedPerson.getLastName());
        assertEquals("São Petesburgo - Russia", foundedPerson.getAddress());
        assertEquals("Male", foundedPerson.getGender());
    }

    @Test
    @Order(4)
    public void testFindById() throws JsonProcessingException {

        var content =
                given()
                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_JSON)
                        .header(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_ERUDIO)
                        .pathParams("id", person.getId())
                        .when()
                        .get("{id} ")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();
        PersonVO foundedPerson = objectMapper.readValue(content, PersonVO.class);
        person = foundedPerson;

        assertNotNull(foundedPerson);
        assertNotNull(foundedPerson.getId());
        assertNotNull(foundedPerson.getFirstName());
        assertNotNull(foundedPerson.getLastName());
        assertNotNull(foundedPerson.getAddress());
        assertNotNull(foundedPerson.getGender());
        assertFalse(foundedPerson.getEnabled());

        assertTrue(foundedPerson.getId() > 0);
        assertEquals("Ródia", foundedPerson.getFirstName());
        assertEquals("Raskólnikov", foundedPerson.getLastName());
        assertEquals("São Petesburgo - Russia", foundedPerson.getAddress());
        assertEquals("Male", foundedPerson.getGender());
    }

    @Test
    @Order(5)
    public void testDelete() throws JsonProcessingException {
        given()
                .spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .pathParam("id", person.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(6)
    public void testFindByName() throws JsonProcessingException {

        var contentString =
                given()
                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_JSON)
                        .queryParams("page", 0, "size", 6, "direction", "asc")
                        .pathParam("firstName", "ayr")
                        .when()
                        .get("/findByName/{firstName}")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();

        WrapperPersonVO wrapper = objectMapper.readValue(contentString, WrapperPersonVO.class);
        var people = wrapper.getEmbedded().getPersonVOList();

        var foundPerson = people.getFirst();

        assertNotNull(foundPerson.getId());
        assertNotNull(foundPerson.getFirstName());
        assertNotNull(foundPerson.getLastName());
        assertNotNull(foundPerson.getAddress());
        assertNotNull(foundPerson.getGender());
        assertTrue(foundPerson.getEnabled());
        assertEquals(1, foundPerson.getId());
        assertEquals("Ayrton", foundPerson.getFirstName());
        assertEquals("Senna", foundPerson.getLastName());
        assertEquals("São Paulo", foundPerson.getAddress());
        assertEquals("Male", foundPerson.getGender());
    }

    @Test
    @Order(7)
    public void testFindAll() throws JsonProcessingException {

        var contentString =
                given()
                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_JSON)
                        .queryParams("page", 3, "size", 10, "direction", "asc")
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();

        WrapperPersonVO wrapper = objectMapper.readValue(contentString, WrapperPersonVO.class);
        var people = wrapper.getEmbedded().getPersonVOList();

        var foundPersonOne = people.getFirst();

        assertNotNull(foundPersonOne.getId());
        assertNotNull(foundPersonOne.getFirstName());
        assertNotNull(foundPersonOne.getLastName());
        assertNotNull(foundPersonOne.getAddress());
        assertNotNull(foundPersonOne.getGender());
        assertTrue(foundPersonOne.getEnabled());
        assertEquals(677, foundPersonOne.getId());
        assertEquals("Alic", foundPersonOne.getFirstName());
        assertEquals("Terbrug", foundPersonOne.getLastName());
        assertEquals("3 Eagle Crest Court", foundPersonOne.getAddress());
        assertEquals("Male", foundPersonOne.getGender());

        var foundPersonFive = people.get(3);

        assertNotNull(foundPersonFive.getId());
        assertNotNull(foundPersonFive.getFirstName());
        assertNotNull(foundPersonFive.getLastName());
        assertNotNull(foundPersonFive.getAddress());
        assertNotNull(foundPersonFive.getGender());
        assertFalse(foundPersonFive.getEnabled());
        assertEquals(409, foundPersonFive.getId());
        assertEquals("Alister", foundPersonFive.getFirstName());
        assertEquals("Etheridge", foundPersonFive.getLastName());
        assertEquals("333 Lakewood Gardens Street", foundPersonFive.getAddress());
        assertEquals("Male", foundPersonFive.getGender());

        var foundPersonTen = people.get(6);

        assertNotNull(foundPersonTen.getId());
        assertNotNull(foundPersonTen.getFirstName());
        assertNotNull(foundPersonTen.getLastName());
        assertNotNull(foundPersonTen.getAddress());
        assertNotNull(foundPersonTen.getGender());
        assertFalse(foundPersonTen.getEnabled());
        assertEquals(797, foundPersonTen.getId());
        assertEquals("Allin", foundPersonTen.getFirstName());
        assertEquals("Emmot", foundPersonTen.getLastName());
        assertEquals("7913 Lindbergh Way", foundPersonTen.getAddress());
        assertEquals("Male", foundPersonTen.getGender());
    }

    @Test
    @Order(8)
    public void testFindAllWithoutToken() throws JsonProcessingException {

        var specificationWithoutToken = new RequestSpecBuilder()
                .setBasePath("/api/people/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        given()
                .spec(specificationWithoutToken)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .when()
                .get()
                .then()
                .statusCode(403);
    }

    @Test
    @Order(9)
    public void testHATEOAS() throws JsonProcessingException {

        var content =
                given()
                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_JSON)
                        .queryParams("page", 3, "size", 10, "direction", "asc")
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();

        assertTrue(content.contains("\"_links\":{\"self\":{\"href\":\"http://localhost:8888/api/people/v1/677\"}}"));
        assertTrue(content.contains("\"_links\":{\"self\":{\"href\":\"http://localhost:8888/api/people/v1/199\"}}"));
        assertTrue(content.contains("\"_links\":{\"self\":{\"href\":\"http://localhost:8888/api/people/v1/687\"}}"));

        assertTrue(content.contains("{\"first\":{\"href\":\"http://localhost:8888/api/people/v1?direction=asc&page=0&size=10&sort=firstName,asc\"}"));
        assertTrue(content.contains("\"prev\":{\"href\":\"http://localhost:8888/api/people/v1?direction=asc&page=2&size=10&sort=firstName,asc\"}"));
        assertTrue(content.contains("\"self\":{\"href\":\"http://localhost:8888/api/people/v1?page=3&size=10&direction=asc\"}"));
        assertTrue(content.contains("\"next\":{\"href\":\"http://localhost:8888/api/people/v1?direction=asc&page=4&size=10&sort=firstName,asc\"}"));
        assertTrue(content.contains("\"last\":{\"href\":\"http://localhost:8888/api/people/v1?direction=asc&page=100&size=10&sort=firstName,asc\"}}"));

        assertTrue(content.contains("\"page\":{\"size\":10,\"totalElements\":1007,\"totalPages\":101,\"number\":3}"));


    }

    private void mockPerson() {
        person.setFirstName("Rodion");
        person.setLastName("Raskólnikov");
        person.setAddress("São Petesburgo - Russia");
        person.setGender("Male");
        person.setEnabled(true);
    }

}