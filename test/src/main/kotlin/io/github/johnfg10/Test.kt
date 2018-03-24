package io.github.johnfg10

import io.github.johnfg10.vertxanno.VertxAnno
import io.github.johnfg10.vertxanno.annotations.Controller
import io.github.johnfg10.vertxanno.annotations.http.HTTP
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

fun main(args: Array<String>) {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(VertxAnno())

}

@Controller()
class Test{
    @HTTP(HttpMethod.GET)
    fun test(routingContext: RoutingContext){
        routingContext.response().end("hello")
    }

    @HTTP(HttpMethod.GET, "/test")
    fun test1(routingContext: RoutingContext){
        routingContext.response().end("world")
    }
}

@Controller("/testa")
class Test1{
    @HTTP(HttpMethod.GET)
    fun test(routingContext: RoutingContext){
        routingContext.response().end("hello")
    }

    @HTTP(HttpMethod.GET, "/test")
    fun test1(routingContext: RoutingContext){
        routingContext.response().end("world")
    }
}