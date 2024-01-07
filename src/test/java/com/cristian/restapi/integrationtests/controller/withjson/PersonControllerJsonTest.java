package com.cristian.restapi.integrationtests.controller.withjson;

import com.cristian.restapi.configs.TestConfigs;
import com.cristian.restapi.integrationtests.testcontainers.AbstractIntegrationTest;
import com.cristian.restapi.integrationtests.vo.PersonVO;
import com.cristian.restapi.integrationtests.vo.security.AccountCredentialVO;
import com.cristian.restapi.integrationtests.vo.security.TokenVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

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
    public void testFindAll() throws JsonProcessingException {

        var contentString =
                given()
                        .spec(specification)
                        .contentType(TestConfigs.CONTENT_TYPE_JSON)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();

        List<PersonVO> people = objectMapper.readValue(contentString, new TypeReference<List<PersonVO>>() {
        });

        var foundPersonOne = people.getFirst();

        assertNotNull(foundPersonOne.getId());
        assertNotNull(foundPersonOne.getFirstName());
        assertNotNull(foundPersonOne.getLastName());
        assertNotNull(foundPersonOne.getAddress());
        assertNotNull(foundPersonOne.getGender());
        assertEquals(1, foundPersonOne.getId());
        assertEquals("Ayrton", foundPersonOne.getFirstName());
        assertEquals("Senna", foundPersonOne.getLastName());
        assertEquals("São Paulo", foundPersonOne.getAddress());
        assertEquals("Male", foundPersonOne.getGender());

        var foundPersonFive = people.get(3);

        assertNotNull(foundPersonFive.getId());
        assertNotNull(foundPersonFive.getFirstName());
        assertNotNull(foundPersonFive.getLastName());
        assertNotNull(foundPersonFive.getAddress());
        assertNotNull(foundPersonFive.getGender());
        assertEquals(5, foundPersonFive.getId());
        assertEquals("Mahatma", foundPersonFive.getFirstName());
        assertEquals("Gandhi", foundPersonFive.getLastName());
        assertEquals("Porbandar - India", foundPersonFive.getAddress());
        assertEquals("Male", foundPersonFive.getGender());

        var foundPersonTen = people.get(6);

        assertNotNull(foundPersonTen.getId());
        assertNotNull(foundPersonTen.getFirstName());
        assertNotNull(foundPersonTen.getLastName());
        assertNotNull(foundPersonTen.getAddress());
        assertNotNull(foundPersonTen.getGender());
        assertEquals(10, foundPersonTen.getId());
        assertEquals("Nikola", foundPersonTen.getFirstName());
        assertEquals("Tesla", foundPersonTen.getLastName());
        assertEquals("Smiljan - Croácia", foundPersonTen.getAddress());
        assertEquals("Male", foundPersonTen.getGender());
    }

    @Test
    @Order(7)
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

    private void mockPerson() {
        person.setFirstName("Rodion");
        person.setLastName("Raskólnikov");
        person.setAddress("São Petesburgo - Russia");
        person.setGender("Male");
        person.setEnabled(true);
    }

}