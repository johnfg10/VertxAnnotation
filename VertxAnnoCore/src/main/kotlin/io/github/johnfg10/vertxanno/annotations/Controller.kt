package io.github.johnfg10.vertxanno.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Controller(val path: String = "/")