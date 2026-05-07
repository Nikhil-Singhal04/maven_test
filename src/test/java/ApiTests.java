import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import io.restassured.RestAssured;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class ApiTests {

    private static WireMockServer wireMockServer;

    private static final String USERS_LIST_JSON =
            "{ \"page\":1, \"data\":[{\"id\":1,\"email\":\"george.bluth@reqres.in\",\"first_name\":\"George\"}]}";

    private static final String SINGLE_USER_JSON =
            "{ \"data\":{\"id\":2,\"email\":\"janet.weaver@reqres.in\",\"first_name\":\"Janet\"}}";

    private static final String CREATED_USER_JSON =
            "{ \"name\":\"John Doe\", \"job\":\"QA Engineer\", \"id\":\"123\", \"createdAt\":\"2026-01-01T00:00:00.000Z\" }";

    @BeforeAll
    public static void setup() {

        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();

        configureFor("localhost", wireMockServer.port());

        // GET users
        wireMockServer.stubFor(get(urlEqualTo("/api/users?page=1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(USERS_LIST_JSON)));

        // GET single user
        wireMockServer.stubFor(get(urlEqualTo("/api/users/2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(SINGLE_USER_JSON)));

        // POST user
        wireMockServer.stubFor(post(urlEqualTo("/api/users"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(CREATED_USER_JSON)));

        // DELETE user
        wireMockServer.stubFor(delete(urlEqualTo("/api/users/2"))
                .willReturn(aResponse()
                        .withStatus(204)));

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = wireMockServer.port();
    }

    @AfterAll
    public static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    // ---------------------------------------------------
    // GET all users
    // ---------------------------------------------------
    @Test
    public void testGetUsers() {

        given()
                .queryParam("page", 1)

                .when()
                .get("/api/users")

                .then()
                .statusCode(200)
                .body("data.size()", greaterThan(0))
                .body("data[0].id", equalTo(1))
                .body("data[0].email", containsString("@"));
    }

    // ---------------------------------------------------
    // GET single user
    // ---------------------------------------------------
    @Test
    public void testGetSingleUser() {

        given()

                .when()
                .get("/api/users/2")

                .then()
                .statusCode(200)
                .body("data.id", equalTo(2))
                .body("data.email", containsString("@"))
                .body("data.first_name", equalTo("Janet"));
    }

    // ---------------------------------------------------
    // POST user
    // ---------------------------------------------------
    @Test
    public void testCreateUser() {

        String requestBody = """
                {
                    "name": "John Doe",
                    "job": "QA Engineer"
                }
                """;

        given()
                .header("Content-Type", "application/json")
                .body(requestBody)

                .when()
                .post("/api/users")

                .then()
                .statusCode(201)
                .body("name", equalTo("John Doe"))
                .body("job", equalTo("QA Engineer"))
                .body("id", notNullValue())
                .body("createdAt", notNullValue());
    }

    // ---------------------------------------------------
    // DELETE user
    // ---------------------------------------------------
    @Test
    public void testDeleteUser() {

        given()

                .when()
                .delete("/api/users/2")

                .then()
                .statusCode(204);
    }
}