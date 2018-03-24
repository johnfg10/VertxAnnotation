package io.github.johnfg10.vertxanno.annotations.fieldannotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ConsumesMimeType(val consumesMimeType: String)