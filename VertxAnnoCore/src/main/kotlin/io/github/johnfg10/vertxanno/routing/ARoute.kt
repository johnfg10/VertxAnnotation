package io.github.johnfg10.vertxanno.routing

import io.vertx.core.http.HttpMethod
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.Route
import kotlin.reflect.KFunction

data class ARoute(
        // class reference
        val inst: Any,
        val kFunction: KFunction<*>,
        // if null we will match to any HTTP method
        val httpMethod: HttpMethod? = null,
        //the path on which this route exists, if empty will match to any
        val path: String = "",
        // whether or not the path should use regex
        val pathUsesRegex: Boolean = false,
        // the mime types the route accepts
        val consumesMimeType: String? = null,
        // the mime types that the client must accept(so the client must accept at least one of these)
        val producesMimeType: String? = null,
        // an optional value signifying the Authentication provider
        val authProvider: AuthProvider? = null,
        // whether or not the rout is enabled
        val isEnabled: Boolean = true,
        val enableCookies: Boolean = false,
        val enableSessions: Boolean = false,
        val isBlocking: Boolean = false,
        var route: Route? = null
)