package giri.api

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import groovy.json.JsonSlurper
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.specification.RequestSpecification
import org.junit.Rule
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.restassured3.RestAssuredRestDocumentation

import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.JsonFieldType


@Integration
@Rollback
class ApiDocumentationArtistSpecSpec extends GebSpec {

    static final String LOGIN_ENDPOINT = '/api/login'
    static final String ARTISTS_ENDPOINT = '/api/artists'

    @Value('${local.server.port}')
    protected int port

    @Rule
    protected JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation('build/docs/generated-snippets')

    private RequestSpecification documentationSpec

    def setup() {
        this.documentationSpec = new RequestSpecBuilder().addFilter(
            documentationConfiguration(this.restDocumentation))
            .build()
    }

    def cleanup() {}

    protected String authenticateUser(String username, String password) {
        String authResponse = RestAssured.given()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(""" {"username" : "$username", "password" : "$password"} """)
            .when()
            .port(this.port)
            .post(LOGIN_ENDPOINT)
            .body()
            .asString()
        return new JsonSlurper().parseText(authResponse).'access_token'
    }

    void "Test and document list Artists request (GET request, index action) to end-point: /api/artists"() {
        given: "An Artist is created by Admin in the system"
        String accessToken = authenticateUser('admin', 'admin')
        RestAssured.given()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("X-Auth-Token", "${accessToken}")
            .body("""{ "firstName" : "Giridhar", "lastName" : "Pottepalem" }""")
            .when()
            .port(this.port)
            .post(ARTISTS_ENDPOINT)
            .then().assertThat().statusCode(HttpStatus.CREATED.value())

        and: "request specification for documenting list Artists API"
        RequestSpecification requestSpecification = RestAssured.given(this.documentationSpec)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .filter(
                RestAssuredRestDocumentation.document(
                    'artists-list-example',
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath('[]').type(JsonFieldType.ARRAY).description('Artists list'),
                        PayloadDocumentation.fieldWithPath('[].id').type(JsonFieldType.STRING).description('Artist id'),
                        PayloadDocumentation.fieldWithPath('[].firstName').type(JsonFieldType.STRING).description('Artist first name'),
                        PayloadDocumentation.fieldWithPath('[].lastName').type(JsonFieldType.STRING).description('Artist last name'),
                        PayloadDocumentation.fieldWithPath('[].createdAt').type(JsonFieldType.STRING).description("Creation date (dd/MM/yyyy)"),
                        PayloadDocumentation.fieldWithPath('[].updatedAt').type(JsonFieldType.STRING).description("Update date (dd/MM/yyyy)")
                    )
                )
            )

        when: "get request is made to end-point"
        def response = requestSpecification
            .when()
            .port(this.port)
            .get(ARTISTS_ENDPOINT)

        then: "status is OK"
        response.then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
    }

    void "Test and document show Artist request (GET request, show action) to end-point: /api/artists"() {
        given: "Pick an artist to show"
        Artist artist = Artist.first()

        and: "user logs in"
        String accessToken = authenticateUser('me', 'password')

        and: "documentation specification"
        RequestSpecification requestSpecification = RestAssured.given(this.documentationSpec)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .filter(
                RestAssuredRestDocumentation.document(
                    'artists-retrieve-specific-example',
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath('id').type(JsonFieldType.STRING).description('Artist id'),
                        PayloadDocumentation.fieldWithPath('firstName').type(JsonFieldType.STRING).description('Artist first name'),
                        PayloadDocumentation.fieldWithPath('lastName').type(JsonFieldType.STRING).description('Artist last name'),
                        PayloadDocumentation.fieldWithPath('createdAt').type(JsonFieldType.STRING).description("Creation date (dd/MM/yyyy)"),
                        PayloadDocumentation.fieldWithPath('updatedAt').type(JsonFieldType.STRING).description("Update date (dd/MM/yyyy)")
                    )
                )
            )

        when: "GET request is sent"
        def response = requestSpecification
            .header("X-Auth-Token", "${accessToken}")
            .when()
            .port(this.port)
            .get("${ARTISTS_ENDPOINT}/${artist.id}")

        def responseJson = new JsonSlurper().parseText(response.body().asString())

        then:
        response.then().assertThat().statusCode(HttpStatus.OK.value())

        and:
        responseJson.id
    }

    void "Test and document create Artist request (POST request, save action) to end-point: /api/artists"() {
        given:
        Artist.withNewTransaction {
            Artist.executeUpdate("delete from Artist")
        }
        int nArtists = 0

        and:
        String accessToken = authenticateUser('admin', 'admin')

        and:
        RequestSpecification requestSpecification = RestAssured.given(this.documentationSpec)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .filter(
                RestAssuredRestDocumentation.document(
                    'artists-create-example',
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath('firstName').description('Artist first name'),
                        PayloadDocumentation.fieldWithPath('lastName').description('Artist last name')
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath('id').type(JsonFieldType.STRING).description('Artist id'),
                        PayloadDocumentation.fieldWithPath('firstName').type(JsonFieldType.STRING).description('Artist first name'),
                        PayloadDocumentation.fieldWithPath('lastName').type(JsonFieldType.STRING).description('Artist last name'),
                        PayloadDocumentation.fieldWithPath('createdAt').type(JsonFieldType.STRING).description("Creation date (dd/MM/yyyy)"),
                        PayloadDocumentation.fieldWithPath('updatedAt').type(JsonFieldType.STRING).description("Update date (dd/MM/yyyy)")
                    )
                )
            )

        when:
        def response = requestSpecification
            .header("X-Auth-Token", "${accessToken}")
            .body("""{ "firstName" : "Bhuvan", "lastName" : "Pottepalem" }""")
            .when()
            .port(this.port)
            .post(ARTISTS_ENDPOINT)

        def responseJson = new JsonSlurper().parseText(response.body().asString())

        then:
        response.then().assertThat().statusCode(HttpStatus.CREATED.value())

        and:
        responseJson.id

        and:
        Artist.count() == nArtists + 1
    }

    void "Test and document update Artist request (PUT request, update action) to end-point: /api/artists"() {
        given:
        Artist artist = Artist.first()

        and:
        RequestSpecification requestSpecification = RestAssured.given(this.documentationSpec)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .filter(
                RestAssuredRestDocumentation.document(
                    'artists-update-example',
                    PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath('lastName').description('Updated last name')
                    ),
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath('id').type(JsonFieldType.STRING).description('Artist id'),
                        PayloadDocumentation.fieldWithPath('firstName').type(JsonFieldType.STRING).description('Artist first name'),
                        PayloadDocumentation.fieldWithPath('lastName').type(JsonFieldType.STRING).description('Updated last name'),
                        PayloadDocumentation.fieldWithPath('createdAt').type(JsonFieldType.STRING).description("Creation date (dd/MM/yyyy)"),
                        PayloadDocumentation.fieldWithPath('updatedAt').type(JsonFieldType.STRING).description("Update date (dd/MM/yyyy)")
                    )
                )
            )

        when:
        String accessToken = authenticateUser('me', 'password')

        def response = requestSpecification
            .header("X-Auth-Token", "${accessToken}")
            .body("""{ "lastName" : "${artist.lastName}(updated)" }""")
            .when()
            .port(this.port)
            .put("${ARTISTS_ENDPOINT}/${artist.id}")

        def responseJson = new JsonSlurper().parseText(response.body().asString())

        then:
        response.then().assertThat().statusCode(HttpStatus.OK.value())

        and:
        responseJson.id == artist.id.toString()
        responseJson.lastName == "${artist.lastName}(updated)"
    }

    // void "Test and document delete Artist request (DELETE request, delete action) to end-point: /api/artists"() {
    //     given:
    //     int nArtists = Artist.count()

    //     and:
    //     Artist artist = Artist.first()

    //     and:
    //     String accessToken = authenticateUser('admin', 'admin')

    //     and:
    //     RequestSpecification requestSpecification = RestAssured.given(this.documentationSpec)
    //         .accept(MediaType.APPLICATION_JSON_VALUE)
    //         .filter(
    //             RestAssuredRestDocumentation.document('artists-delete-example')
    //         )

    //     when:
    //     def response = requestSpecification
    //         .header("X-Auth-Token", "${accessToken}")
    //         .when()
    //         .port(this.port)
    //         .delete("${ARTISTS_ENDPOINT}/${artist.id}")

    //     then:
    //     response.then().assertThat().statusCode(HttpStatus.NO_CONTENT.value())

    //     and:
    //     Artist.count() == nArtists - 1

    //     and:
    //     !Artist.exists(artist.id)
    // }
}
