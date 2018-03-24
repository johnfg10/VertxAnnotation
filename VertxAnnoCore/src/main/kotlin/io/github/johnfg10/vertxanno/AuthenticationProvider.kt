package io.github.johnfg10.vertxanno

import io.vertx.ext.auth.AuthProvider

interface AuthenticationProvider {
    fun getAuthProvider() : AuthProvider
}