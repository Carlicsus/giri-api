package giri.api


import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured
import grails.converters.*

class ArtistController extends RestfulController {
    static responseFormats = ['json', 'xml']
    ArtistController() {
        super(Artist)
    }

    @Secured('permitAll')
    @Override
    def index(Integer max) {
        super.index(max)
    }

    @Secured('ROLE_USER')
    @Override
    def show() {
        super.show()
    }

    @Secured('ROLE_ADMIN')
    @Override
    def save() {
        super.save()
    }

    @Secured('ROLE_USER')
    @Override
    def update() {
        super.update()
    }

    @Secured('ROLE_ADMIN')
    @Override
    def delete() {
        super.delete()
    }
}
