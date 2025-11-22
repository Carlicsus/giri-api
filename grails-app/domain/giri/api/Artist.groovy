package giri.api


import grails.rest.*
import grails.plugin.springsecurity.annotation.Secured

@Resource(uri = '/api/artists', readOnly = false, formats = ['json', 'xml'])
@Secured(['ROLE_ADMIN'])
class Artist {
    String id
    String firstName
    String lastName
    Date dateCreated
    Date lastUpdated

    static constraints = {
        id size: 32..32, unique: true
        firstName blank: false
        lastName blank: false
    }

    static mapping = {
        id generator: 'uuid'
    }

}