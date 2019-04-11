package com.example.starter

import io.vertx.core.DeploymentOptions
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
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
  companion object {
    const val PORT: Int = 8000
    const val HOST: String = "localhost"
    val LOGGER: Logger = LoggerFactory.getLogger(TestMainVerticle::class.java)
  }


  fun deployMain(vertx: Vertx) = vertx.rxDeployVerticle(
    MainVerticle(), DeploymentOptions().setConfig(
      json {
        obj(
          "verticle" to obj(
            "rest" to obj(
              "cn" to RestServerVerticle::class.java.canonicalName,
              "host" to HOST,
              "port" to PORT
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
  @DisplayName("Should start a Web Server on port $PORT and send message to DAO")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  @Throws(Throwable::class)
  fun start_http_server(vertx: Vertx, testContext: VertxTestContext) {
    val messages = mutableListOf<ModelExample>()
    vertx.eventBus()
      .consumer<ModelExample>("dao.test")
      .handler { message ->
        messages.add(message.body())
        LOGGER.info("message sent to dao")
        message.reply(null)
      }
      .rxCompletionHandler()
      .andThen(deployMain(vertx))
      .subscribe { _ ->
        assertEquals(3, vertx.delegate.deploymentIDs().size)
        LOGGER.info("deployed 3 verticles")
        WebClient.create(vertx).post(PORT, HOST, "/message").rxSendJson(
          json {
            obj(
              "name" to "o meu nome",
              "code" to 123
            )
          }
        ).subscribe({ response ->
          testContext.verify {
            LOGGER.info("post reply")
            assertEquals(1, messages.size)
            assertEquals("o meu nome", messages[0].name)
            assertEquals(123, messages[0].code)
            assertEquals(204, response.statusCode())
            testContext.completeNow()
          }
        }, testContext::failNow)
      }
  }

}
