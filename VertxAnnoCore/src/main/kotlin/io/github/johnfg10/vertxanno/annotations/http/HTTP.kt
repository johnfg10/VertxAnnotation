package io.github.johnfg10.vertxanno.annotations.http

import io.vertx.core.http.HttpMethod

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class HTTP(val httpMethod: HttpMethod = HttpMethod.OTHER, val path: String = "/",
/* not normally used but is used when httpMethod == OTHER */val httpOther: String = "UNSET",
                      val useRegex: Boolean = false,
                      val isEnabled: Boolean = false
)