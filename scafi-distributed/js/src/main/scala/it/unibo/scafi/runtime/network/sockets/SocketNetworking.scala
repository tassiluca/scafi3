package it.unibo.scafi.runtime.network.sockets

import java.util.concurrent.atomic.AtomicBoolean

import scala.scalajs.js
import scala.scalajs.js.typedarray.{ DataView, Uint8Array }
import scala.util.boundary
import scala.util.boundary.break
import scala.util.chaining.scalaUtilChainingOps
import scala.concurrent.{ ExecutionContext, Future, Promise }

import it.unibo.scafi.runtime.network.Serializable
import it.unibo.scafi.runtime.network.Serializable.*

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
trait SocketNetworking[Message: Serializable](using ec: ExecutionContext, conf: SocketConfiguration)
    extends NetworkingTemplate[Message]:

  object Events:
    val connect = "connect"
    val error = "error"
    val listening = "listening"
    val data = "data"

  import Events.*

  override def out(endpoint: Endpoint): () => Future[Connection] = () =>
    for
      socket <- createSocket(endpoint)
      conn = new ConnectionTemplate:
        override def write(buffer: Array[Byte]): Future[Unit] = Promise[Unit]().pipe: p =>
          val data = new Uint8Array(buffer.length)
          for i <- buffer.indices do data(i) = buffer(i)
          socket.write(data)(Option(_).fold(p.trySuccess(()))(err => p.tryFailure(Exception(err))))
          p.future
        override def isOpen: Boolean = !socket.destroyed
        override def close(): Unit = socket.destroy()
    yield conn

  private def createSocket(endpoint: Endpoint) = Promise[Socket]().pipe: p =>
    val socket = Net.connect(endpoint._2, endpoint._1)
    socket.on(connect)(_ => p.trySuccess(socket): Unit)
    socket.on(error): err =>
      p.tryFailure(Exception(err.asInstanceOf[js.Dynamic].message.toString)): Unit
      socket.destroy()
    p.future

  override def in(port: Port)(onReceive: Message => Unit): () => Future[ListenerRef] = () =>
    val connListenerPromise = Promise[Listener]()
    val acceptPromise = Promise[Unit]()
    def react(data: Uint8Array): Unit = boundary:
      var offset = 0
      while data.byteLength - offset >= 4 do
        val view = new DataView(data.buffer, data.byteOffset + offset, data.byteLength - offset)
        val msgLen = view.getUint32(0).toInt
        if msgLen <= 0 || msgLen > data.byteLength - offset - 4 || msgLen > conf.maxMessageSize then break(())
        val messageBytes = new Uint8Array(data.buffer, data.byteOffset + offset + 4, msgLen)
        val bytes = new Array[Byte](msgLen)
        for i <- 0 until msgLen do bytes(i) = messageBytes(i).toByte
        onReceive(deserialize(bytes))
        offset += 4 + msgLen
    val serverSocket = Net.createServer(_.on(data)(data => react(data.asInstanceOf[Uint8Array])))
    val listener = new Listener:
      private val open = AtomicBoolean(true)
      override def boundPort: Port = serverSocket.address().port
      override def close(): Unit =
        open.set(false)
        serverSocket.close()
        acceptPromise.trySuccess(()): Unit
      override def isOpen: Boolean = open.get()
    serverSocket.on(listening)(_ => connListenerPromise.trySuccess(listener): Unit)
    serverSocket.on(error): err =>
      connListenerPromise.tryFailure(Exception(err.asInstanceOf[scala.scalajs.js.Dynamic].message.toString)): Unit
      serverSocket.close()
    serverSocket.listen(port)
    connListenerPromise.future.map(ListenerRef(_, acceptPromise.future))
end SocketNetworking
