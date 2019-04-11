package com.example.starter

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

fun main(args: Array<String>) {
  val vertx = Vertx.vertx()

  vertx.deployVerticle(
    MainVerticle(), DeploymentOptions().setConfig(
      json {
        obj(
          "verticle" to obj(
            "rest" to obj(
              "cn" to RestServerVerticle::class.java.canonicalName,
              "host" to "localhost",
              "port" to 8888
            ),
            "dao" to obj(
              "cn" to DaoExample::class.java.canonicalName
            )
          )
        )
      }
    )
  )
}
