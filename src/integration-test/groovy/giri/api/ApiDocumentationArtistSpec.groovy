package giri.api

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.*
import spock.lang.*
import grails.plugins.rest.client.RestBuilder
import grails.transaction.Rollback
import io.restassured.builder.RequestSpecBuilder
import io.restassured.specification.RequestSpecification
import org.junit.Rule
import org.springframework.beans.factory.annotation.Value
import org.springframework.restdocs.JUnitRestDocumentation

import static org.springframework.http.HttpStatus.*
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration

@Integration
@Rollback
class ApiDocumentationArtistSpec extends Specification    {
    static final String LOGIN_ENDPOINT = '/api/login'
    static final String ARTISTS_ENDPOINT = '/api/artists'

    @Value('${local.server.port}')
    protected int port

    @Rule
    protected JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation('build/docs/generated-snippets')

    private RequestSpecification documentationSpec

    def setupSpec() {
    }

    def setup() {
        //set documentation specification
        this.documentationSpec = new RequestSpecBuilder().addFilter(
            documentationConfiguration(this.restDocumentation))
            .build()
    }

    void "debug port"() {
        println "PORT ES = ${port}"
        expect:
        true
    }


    
}