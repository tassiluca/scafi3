package it.unibo.scafi.runtime.network.sockets

import java.util.concurrent.atomic.AtomicBoolean
import java.nio.ByteBuffer

import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array
import scala.util.boundary
import scala.util.boundary.break
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.collection.mutable.ArrayBuffer

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
        override def write(buffer: Array[Byte]): Future[Unit] =
          val p = Promise[Unit]
          val data = new Uint8Array(buffer.length)
          for i <- buffer.indices do data(i) = buffer(i)
          socket.write(data)(Option(_).fold(p.trySuccess(()))(err => p.tryFailure(Exception(err))))
          p.future
        override def isOpen: Boolean = !socket.destroyed
        override def close(): Unit = socket.destroy()
    yield conn

  private def createSocket(endpoint: Endpoint): Future[Socket] =
    val p = Promise[Socket]
    val socket = Net.connect(endpoint._2, endpoint._1)
    socket.on(connect)(_ => p.trySuccess(socket): Unit)
    socket.on(error): err =>
      p.tryFailure(Exception(err.asInstanceOf[js.Dynamic].message.toString)): Unit
      socket.destroy()
    p.future

  override def in(port: Port)(onReceive: Message => Unit): () => Future[ListenerRef] = () =>
    val acceptPromise = Promise[Unit]()

    val buffer = ArrayBuffer[Byte]()

    def serve(socket: Socket, chunk: Uint8Array): Unit = boundary:
      for i <- 0 until chunk.length do buffer += chunk(i).toByte
      while buffer.length >= 4 do
        val msgLen = buffer.slice(0, 4).toArray
        val length = ByteBuffer.wrap(msgLen).getInt()
        if length > conf.maxMessageSize then
          socket.destroy()
          break(())
        else if buffer.length >= 4 + length then
          val msgBytes = buffer.slice(4, 4 + length).toArray
          buffer.remove(0, 4 + length)
          onReceive(deserialize(msgBytes))
        else break(())

    val serverSocket = Net.createServer(socket => socket.on(data)(data => serve(socket, data.asInstanceOf[Uint8Array])))

    val listener = new Listener:
      private val open = AtomicBoolean(true)
      override def boundPort: Port = serverSocket.address().port
      override def close(): Unit =
        open.set(false)
        serverSocket.close()
        acceptPromise.trySuccess(()): Unit
      override def isOpen: Boolean = open.get()

    val connListenerPromise = Promise[Listener]()
    serverSocket.on(listening)(_ => connListenerPromise.trySuccess(listener): Unit)
    serverSocket.on(error): err =>
      connListenerPromise.tryFailure(Exception(err.asInstanceOf[scala.scalajs.js.Dynamic].message.toString)): Unit
      serverSocket.close()
    serverSocket.listen(port)
    connListenerPromise.future.map(ListenerRef(_, acceptPromise.future))
end SocketNetworking
