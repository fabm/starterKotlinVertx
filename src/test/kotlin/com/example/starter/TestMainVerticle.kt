package com.example.starter

import io.vertx.core.DeploymentOptions
import io.vertx.junit5.Timeout
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.client.WebClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@ExtendWith(VertxExtension::class)
class TestMainVerticle {

  val port: Int = 8888
  val host: String = "localhost"

  fun deployMain(vertx: Vertx) = vertx.rxDeployVerticle(
    MainVerticle(), DeploymentOptions().setConfig(
      json {
        obj(
          "verticle" to obj(
            "rest" to obj(
              "cn" to RestServerVerticle::class.java.canonicalName,
              "host" to host,
              "port" to port
            ),
            "dao" to obj(
              "cn" to DaoTest::class.java.canonicalName
            )
          )
        )
      }
    )
  )

  @Test
  @DisplayName("Should start a Web Server on port 8888")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  @Throws(Throwable::class)
  fun start_http_server(vertx: Vertx, testContext: VertxTestContext) {
    var messages = mutableListOf<ModelExample>()
    vertx.eventBus()
      .consumer<ModelExample>("dao.test")
      .handler { message ->
        messages.add(message.body())
        println("message sent to dao")
        message.reply(null)
      }
      .rxCompletionHandler()
      .andThen(deployMain(vertx))
      .subscribe { it ->
        assertEquals(3, vertx.delegate.deploymentIDs().size)
        println("deployed 3 verticles")
        val nome =
        WebClient.create(vertx).post(port, host, "/message").rxSendJson(
          json {
            obj(
              "name" to "o meu nome",
              "code" to 123
            )
          }
        ).subscribe({ response ->
          testContext.verify {
            println("post reply")
            assertEquals(1, messages.size)
            assertEquals(204, response.statusCode())
            testContext.completeNow()
          }
        }, testContext::failNow)
      }
  }

}
