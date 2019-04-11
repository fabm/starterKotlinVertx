package com.example.starter

import io.vertx.core.DeploymentOptions
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.core.logging.LoggerFactory
import io.vertx.reactivex.core.AbstractVerticle

class GeneralMessageCodec<T>(val klass: Class<T>) : MessageCodec<T, T> {
  override fun decodeFromWire(pos: Int, buffer: Buffer?): T = throw IllegalStateException("not implemented")
  override fun systemCodecID(): Byte = (-1).toByte()
  override fun encodeToWire(buffer: Buffer?, s: T) = throw IllegalStateException("not implemented")
  override fun transform(s: T): T = s
  override fun name(): String = klass.simpleName
}

class MainVerticle : AbstractVerticle() {

  companion object {
      val LOGGER = LoggerFactory.getLogger(MainVerticle::class.java)
  }
  override fun start(startFuture: io.vertx.core.Future<Void>?) {
    vertx.eventBus().delegate.registerDefaultCodec(
      ModelExample::class.java,
      GeneralMessageCodec(ModelExample::class.java)
    )

    val onSuccess: (String) -> Unit = { LOGGER.info("complete") }
    val onError: (Throwable) -> Unit = { LOGGER.info(it) }

    val restVerticleConf = config().getJsonObject("verticle").getJsonObject("rest")
    val daoVerticleConf = config().getJsonObject("verticle").getJsonObject("dao")

    vertx.rxDeployVerticle(daoVerticleConf.getString("cn"), DeploymentOptions().setConfig(daoVerticleConf))
      .doOnSuccess { LOGGER.info("deployed dao") }
      .doOnError { LOGGER.info("impossible to deploy dao") }
      .flatMap {
        vertx.rxDeployVerticle(
          restVerticleConf.getString("cn"),
          DeploymentOptions().setConfig(restVerticleConf))
      }
      .doOnSuccess { LOGGER.info("deployed rest") }
      .doOnError {
        LOGGER.info("impossible to deploy rest")
        it.printStackTrace()
      }
      .doFinally { startFuture?.complete() }
      .subscribe(onSuccess, onError)
  }
}
