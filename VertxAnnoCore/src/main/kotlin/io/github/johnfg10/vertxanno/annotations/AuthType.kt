package io.github.johnfg10.vertxanno.annotations

import io.github.johnfg10.vertxanno.EAuthType

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class AuthType(val EAuthType: EAuthType)