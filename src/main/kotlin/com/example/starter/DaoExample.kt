package com.example.starter

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject

class DaoExample: AbstractVerticle(){
  override fun start(startFuture: Future<Void>?) {
    val eb = vertx.eventBus()
    eb.consumer<Any>("pt.dao").handler {mh->
      val action = mh.body()
      val me = action as ModelExample
        println("message created:"+JsonObject().put("name",me.name).put("code",me.code))
      mh.reply("ok")
    }
    startFuture?.complete()
  }
}
