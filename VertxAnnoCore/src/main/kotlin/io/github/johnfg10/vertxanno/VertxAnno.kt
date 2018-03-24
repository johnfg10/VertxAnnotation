package io.github.johnfg10.vertxanno

import io.github.johnfg10.vertxanno.annotations.AuthType
import io.github.johnfg10.vertxanno.annotations.Controller
import io.github.johnfg10.vertxanno.annotations.EnableCookies
import io.github.johnfg10.vertxanno.annotations.EnableSessions
import io.github.johnfg10.vertxanno.annotations.fieldannotations.Blocking
import io.github.johnfg10.vertxanno.annotations.fieldannotations.ProducesMimeType
import io.github.johnfg10.vertxanno.annotations.fieldannotations.ConsumesMimeType
import io.github.johnfg10.vertxanno.annotations.fieldannotations.Disable
import io.github.johnfg10.vertxanno.annotations.http.*
import io.github.johnfg10.vertxanno.annotations.injectors.routing.RoutingContext
import io.github.johnfg10.vertxanno.routing.ARoute
import io.github.johnfg10.vertxanno.routing.AnnoRoute
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
import io.vertx.core.json.Json
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.oauth2.OAuth2Auth
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions
import io.vertx.ext.auth.oauth2.OAuth2FlowType
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.OAuth2AuthHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

class VertxAnno : AbstractVerticle() {
    private lateinit var server: HttpServer
    private lateinit var router: Router
    private var scanResult: ScanResult? = null
    private val aRouteList = mutableListOf<ARoute>()
    private var authProvider: AuthProvider? = null

    override fun init(vertx: Vertx, context: Context) {
        vertx.executeBlocking<Int>({ future ->
            server = vertx.createHttpServer()
            router = Router.router(vertx)
            server.requestHandler(router::accept).listen(8081)

            future.complete(server.actualPort())
        }, { res ->
            println("The result is: ${res.result()}")
        })

        super.init(vertx, context)
    }

    override fun start(startFuture: Future<Void>?) {
        scanResult = FastClasspathScanner(config().getString("srcpackage", "")).scan()

        if (config().containsKey("auth_provider_class")){
            authProvider = scanResult!!.classNameToClassRef(config().getString("auth_provider_class")).asSubclass(AuthenticationProvider::class.java).newInstance().getAuthProvider()
        }else{
            println("No auth provider configured!")
        }

        val controllerClases = scanResult!!.classNamesToClassRefs(scanResult!!.getNamesOfClassesWithAnnotation(Controller::class.java))

        for (clazz in controllerClases.filter { it.isAnnotationPresent(Controller::class.java) }){
            val controller = clazz::class.findAnnotation<Controller>()
            for (func in clazz.kotlin.functions.filter { it.findAnnotation<HTTP>() != null }){
                val http = func.findAnnotation<HTTP>()!!
                aRouteList.add(ARoute(
                        inst = clazz.newInstance(),
                        kFunction = func,
                        httpMethod = if (http.httpMethod == HttpMethod.OTHER && http.httpOther == "UNSET"){ null } else { http.httpMethod },
                        path = if (controller == null){ http.path } else { controller.path + http.path },
                        pathUsesRegex = http.useRegex,
                        isEnabled = func.findAnnotation<Disable>() != null,
                        producesMimeType =
                        if (func.findAnnotation<ProducesMimeType>() != null){ func.findAnnotation<ProducesMimeType>()!!.producesMimeType }
                        else { null },
                        consumesMimeType =
                        if (func.findAnnotation<ConsumesMimeType>() != null){ func.findAnnotation<ConsumesMimeType>()!!.consumesMimeType }
                        else { null },
                        authProvider =
                        when {
                            func.findAnnotation<AuthType>() != null -> resolveAuthType(func.findAnnotation<AuthType>()!!.EAuthType)
                            clazz.isAnnotationPresent(AuthType::class.java) -> resolveAuthType(clazz.getAnnotation(AuthType::class.java).EAuthType)
                            else -> null
                        },
                        enableCookies = func.findAnnotation<EnableCookies>() != null,
                        enableSessions = func.findAnnotation<EnableSessions>() != null,
                        isBlocking = func.findAnnotation<Blocking>() != null
                 ))
            }
        }

        aRouteList.forEach {
            val route = router.route()

            route.useNormalisedPath(true).path(it.path)

            if (it.httpMethod != null){
                route.method(it.httpMethod)
            }
            if (it.producesMimeType != null){
                route.produces(it.producesMimeType)
            }
            if (it.consumesMimeType != null){
                route.consumes(it.consumesMimeType)
            }
            if (it.enableCookies){
                route.handler(CookieHandler.create())
            }
            if (it.enableSessions){
                //todo intergrate this with the config system
                route.handler(SessionHandler.create(LocalSessionStore.create(vertx)))
            }
            if (it.authProvider != null){

                when(it.authProvider){
                    EAuthType.OAuth2 -> {
                        OAuth2AuthHandler.create(
                                OAuth2Auth.create(vertx,
                                OAuth2FlowType.valueOf(config().getString("oauth2_flow")),
                                OAuth2ClientOptions(config().getJsonObject("oauth2_clientopts"))))
                    }
                }
            }
            it.route = route
            fun execute(context: io.vertx.ext.web.RoutingContext){
                val argList = mutableListOf<Any>()

                it.kFunction.valueParameters.forEach {
                    println("name: ${it.name}, is optional: ${it.isOptional}, ${it.annotations}}")
                    if (it.type.jvmErasure == RoutingContext::class){
                        argList.add(context)
                    }
                }
                it.kFunction.call(it.inst, *argList.toTypedArray())
            }
            if (it.isBlocking){
                route.blockingHandler {
                    execute(it)
                }
            }else {
                route.handler {
                    execute(it)
                }
            }
        }

        super.start(startFuture)
    }

    override fun stop(stopFuture: Future<Void>?) {
        super.stop(stopFuture)
    }

    fun resolveAuthType(EAuthType: EAuthType) : AuthProvider? {
        return null
    }

    fun ScanResult.getNamesOfClassesWithMethodAnnotationAnyOf(vararg annotations: Class<*>): MutableList<String> {
        val classList = mutableListOf<String>()
        annotations.forEach {
            classList.addAll(this.getNamesOfClassesWithFieldAnnotation(it))
        }
        return classList
    }

    fun <T> Collection<T>.containsAny(vararg contains: T): Boolean {
        contains.forEach {
            if (this.contains(it)){
                return true
            }
        }
        return false
    }
}