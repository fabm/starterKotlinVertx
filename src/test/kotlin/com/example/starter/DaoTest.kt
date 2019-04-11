package com.example.starter

import io.vertx.core.Future
import io.vertx.reactivex.core.AbstractVerticle

class DaoTest : AbstractVerticle() {

  override fun start(startFuture: Future<Void>?) {

    vertx.eventBus().consumer<ModelExample>("dao")
      .handler { message ->
        vertx.eventBus().rxSend<ModelExample>("dao.test", message.body()).subscribe{it->
          message.reply("ok")
        }
      }.completionHandler {
        startFuture?.complete()
      }
  }
}

