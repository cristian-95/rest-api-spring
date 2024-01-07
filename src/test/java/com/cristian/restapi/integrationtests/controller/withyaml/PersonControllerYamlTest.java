package com.cristian.restapi.integrationtests.controller.withyaml;

import com.cristian.restapi.configs.TestConfigs;
import com.cristian.restapi.integrationtests.controller.withyaml.mapper.YMLMapper;
import com.cristian.restapi.integrationtests.testcontainers.AbstractIntegrationTest;
import com.cristian.restapi.integrationtests.vo.PersonVO;
import com.cristian.restapi.integrationtests.vo.security.AccountCredentialVO;
import com.cristian.restapi.integrationtests.vo.security.TokenVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonControllerYamlTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static ObjectMapper objectMapper;
    private static YMLMapper ymlMapper;

    private static PersonVO person;

    @BeforeAll
    public static void setup() {

        ymlMapper = new YMLMapper();
        person = new PersonVO();
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

        var persistedPerson =
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
                        .body(person, ymlMapper)
                        .when()
                        .post()
                        .then()
                        .statusCode(201)
                        .extract()
                        .body().as(PersonVO.class, ymlMapper);

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

        var updatedPerson =
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
                        .body(person, ymlMapper)
                        .when()
                        .put()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(PersonVO.class, ymlMapper);


        assertNotNull(updatedPerson);
        assertNotNull(updatedPerson.getId());
        assertNotNull(updatedPerson.getFirstName());
        assertNotNull(updatedPerson.getLastName());
        assertNotNull(updatedPerson.getAddress());
        assertNotNull(updatedPerson.getGender());
        assertTrue(updatedPerson.getEnabled());
        assertEquals(person.getId(), updatedPerson.getId());
        assertEquals("Ródia", updatedPerson.getFirstName());
        assertEquals("Raskólnikov", updatedPerson.getLastName());
        assertEquals("São Petesburgo - Russia", updatedPerson.getAddress());
        assertEquals("Male", updatedPerson.getGender());
    }


    @Test
    @Order(3)
    public void testDisablePersonById() throws JsonProcessingException {

        var foundPerson = given()
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
                .pathParams("id", person.getId())
                .when()
                .patch("{id} ")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(PersonVO.class, ymlMapper);

        assertNotNull(foundPerson);
        assertNotNull(foundPerson.getId());
        assertNotNull(foundPerson.getFirstName());
        assertNotNull(foundPerson.getLastName());
        assertNotNull(foundPerson.getAddress());
        assertNotNull(foundPerson.getGender());
        assertFalse(foundPerson.getEnabled());
        assertTrue(foundPerson.getId() > 0);
        assertEquals("Ródia", foundPerson.getFirstName());
        assertEquals("Raskólnikov", foundPerson.getLastName());
        assertEquals("São Petesburgo - Russia", foundPerson.getAddress());
        assertEquals("Male", foundPerson.getGender());
    }

    @Test
    @Order(4)
    public void testFindById() throws JsonProcessingException {

        var foundPerson = given()
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
                .pathParams("id", person.getId())
                .when()
                .get("{id} ")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(PersonVO.class, ymlMapper);

        assertNotNull(foundPerson);
        assertNotNull(foundPerson.getId());
        assertNotNull(foundPerson.getFirstName());
        assertNotNull(foundPerson.getLastName());
        assertNotNull(foundPerson.getAddress());
        assertNotNull(foundPerson.getGender());
        assertFalse(foundPerson.getEnabled());
        assertTrue(foundPerson.getId() > 0);
        assertEquals("Ródia", foundPerson.getFirstName());
        assertEquals("Raskólnikov", foundPerson.getLastName());
        assertEquals("São Petesburgo - Russia", foundPerson.getAddress());
        assertEquals("Male", foundPerson.getGender());
    }

    @Test
    @Order(5)
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
                .pathParam("id", person.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(6)
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
                        .contentType(TestConfigs.CONTENT_TYPE_YML).accept(TestConfigs.CONTENT_TYPE_YML)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(PersonVO[].class, ymlMapper);

        List<PersonVO> people = Arrays.asList(content);

        var foundPersonOne = people.getFirst();

        assertNotNull(foundPersonOne.getId());
        assertNotNull(foundPersonOne.getFirstName());
        assertNotNull(foundPersonOne.getLastName());
        assertNotNull(foundPersonOne.getAddress());
        assertNotNull(foundPersonOne.getGender());
        assertTrue(foundPersonOne.getEnabled());
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
        assertTrue(foundPersonFive.getEnabled());
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
        assertTrue(foundPersonTen.getEnabled());
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

    private void mockPerson() {
        person.setFirstName("Rodion");
        person.setLastName("Raskólnikov");
        person.setAddress("São Petesburgo - Russia");
        person.setGender("Male");
        person.setEnabled(true);
    }

}