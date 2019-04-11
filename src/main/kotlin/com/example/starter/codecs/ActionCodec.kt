package com.example.starter.codecs

import com.example.starter.Action
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec

class ActionCodec: MessageCodec<Action,Action>{
  override fun decodeFromWire(pos: Int, buffer: Buffer?): Action {
    throw IllegalStateException("not implemented yet")
  }

  override fun systemCodecID(): Byte {
    return (-1).toByte()
  }

  override fun encodeToWire(buffer: Buffer?, s: Action?) {
    throw IllegalStateException("not implemented yet")
  }

  override fun transform(s: Action?): Action {
    return s!!;
  }

  override fun name(): String {
    return ActionCodec::class.java.simpleName
  }

}
