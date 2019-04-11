package com.example.starter

import io.vertx.core.Future
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.core.json.get
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.handler.BodyHandler

class RestServerVerticle : AbstractVerticle() {
  companion object {
    val LOGGER: Logger = LoggerFactory.getLogger(RestServerVerticle::class.java)
  }

  override fun start(startFuture: Future<Void>?) {
    val router = Router.router(vertx)
    router.route().handler(BodyHandler.create())
    router.post("/message").handler { rc ->
      val bodyAsJson = rc.bodyAsJson
      vertx.eventBus().rxSend<ModelExample>("dao", ModelExample(bodyAsJson["name"], bodyAsJson["code"]))
        .subscribe({
          rc.response().statusCode = 204
          rc.response().end()
        }, {
          LOGGER.error("error", it)
          rc.response().statusCode = 500
          rc.response().end()
        })
    }

    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(config().getInteger("port"), config().getString("host")) { http ->
        if (http.succeeded()) {
          LOGGER.info("HTTP server started on port 8888")
          startFuture?.complete()
        } else {
          startFuture?.fail(http.cause());
        }
      }

  }
}
