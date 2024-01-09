package com.cristian.restapi.integrationtests.controller.withyaml;

import com.cristian.restapi.configs.TestConfigs;
import com.cristian.restapi.integrationtests.controller.withyaml.mapper.YMLMapper;
import com.cristian.restapi.integrationtests.testcontainers.AbstractIntegrationTest;
import com.cristian.restapi.integrationtests.vo.PersonVO;
import com.cristian.restapi.integrationtests.vo.pagedmodels.PagedModelPerson;
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
    public void testFindByName() throws JsonProcessingException {

        var wrapper =
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
                        .queryParams("page", 0, "size", 6, "direction", "asc")
                        .pathParam("firstName", "ayr")
                        .when()
                        .get("/findByName/{firstName}")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(PagedModelPerson.class, ymlMapper);


        var people = wrapper.getContent();

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

        var wrapper =
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
                        .queryParams("page", 3, "size", 10, "direction", "asc")
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(PagedModelPerson.class, ymlMapper);


        var people = wrapper.getContent();

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

    @Test
    @Order(9)
    public void testHATEOAS() throws JsonProcessingException {

        var rawContent =
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
                        .queryParams("page", 3, "size", 10, "direction", "asc")
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();

        var content = rawContent.replace("\n", "").replace("\r", "");

        assertTrue(content.contains("- rel: \"first\"  href: \"http://localhost:8888/api/people/v1?direction=asc&page=0&size=10&sort=firstName,asc\""));
        assertTrue(content.contains("- rel: \"prev\"  href: \"http://localhost:8888/api/people/v1?direction=asc&page=2&size=10&sort=firstName,asc\""));
        assertTrue(content.contains("- rel: \"self\"  href: \"http://localhost:8888/api/people/v1?page=3&size=10&direction=asc\""));
        assertTrue(content.contains("- rel: \"next\"  href: \"http://localhost:8888/api/people/v1?direction=asc&page=4&size=10&sort=firstName,asc\""));
        assertTrue(content.contains("- rel: \"last\"  href: \"http://localhost:8888/api/people/v1?direction=asc&page=100&size=10&sort=firstName,asc\""));
        assertTrue(content.contains("links:  - rel: \"self\"    href: \"http://localhost:8888/api/people/v1/677\"  links: []"));
        assertTrue(content.contains("links:  - rel: \"self\"    href: \"http://localhost:8888/api/people/v1/414\"  links: []"));
        assertTrue(content.contains("links:  - rel: \"self\"    href: \"http://localhost:8888/api/people/v1/846\"  links: []"));
        assertTrue(content.contains("page:  size: 10  totalElements: 1008  totalPages: 101  number: 3"));
    }

    private void mockPerson() {
        person.setFirstName("Rodion");
        person.setLastName("Raskólnikov");
        person.setAddress("São Petesburgo - Russia");
        person.setGender("Male");
        person.setEnabled(true);
    }

}