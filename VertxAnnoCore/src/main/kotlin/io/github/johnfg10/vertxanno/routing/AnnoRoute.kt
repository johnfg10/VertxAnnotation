package io.github.johnfg10.vertxanno.routing

import io.github.johnfg10.vertxanno.annotations.Controller
import io.vertx.core.http.HttpMethod
import java.lang.reflect.Method
import java.lang.reflect.Type
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType

data class AnnoRoute(val classInstance: Any, val controllerAnnotation: Controller, val kFunction: KFunction<*>, val methodRoute: String, val HttpMethod: HttpMethod)