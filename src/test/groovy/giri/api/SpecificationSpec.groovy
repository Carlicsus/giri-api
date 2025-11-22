package giri.api

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class SpecificationSpec extends Specification implements DomainUnitTest<Specification> {

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
        expect:"fix me"
        true == false
    }
}
