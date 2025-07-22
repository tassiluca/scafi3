package it.unibo.scafi.runtime.network.sockets

import java.util.concurrent.CopyOnWriteArrayList

import scala.concurrent.Future

import it.unibo.scafi.runtime.network.sockets.InetTypes.*

trait SocketNetworkingBehavior extends NetworkingTest:

  type PlainTextNetworking = Networking & {
    type MessageIn = String
    type MessageOut = String
  }

  def anInboundConnectionListener(using net: Networking): Unit =
    it should "be able to be initialized on a free port" in:
      println(s"[${System.currentTimeMillis()}] Test 1 started")
      val server = net.in(FreePort)(nop)
      val result = server.run() verify: ref =>
        ref.listener.isOpen shouldBe true
        ref.listener.close()
        ref.listener.isOpen shouldBe false
      result.onComplete(_ => println(s"[${System.currentTimeMillis()}] Test 1 completed"))
      result

  def anOutboundConnection(using net: Networking): Unit =
    it should "fail to connect to an unavailable remote endpoint" in:
      println(s"[${System.currentTimeMillis()}] Test 2 started")
      val nonExistentServerPort: Port = 5050
      val client = net.out(Endpoint(Localhost, nonExistentServerPort))
      val result = client.run().failed map:
        _ shouldBe a[Throwable]
      result.onComplete(_ => println(s"[${System.currentTimeMillis()}] Test 2 completed"))
      result

    it should "be able to connect to an available remote endpoint" in:
      println(s"[${System.currentTimeMillis()}] Test 3 started")
      val result = usingServer(nop): client =>
        client.isOpen shouldBe true
        client.close()
        client.isOpen shouldBe false
      result.onComplete(_ => println(s"[${System.currentTimeMillis()}] Test 3 completed"))
      result

  def both(using net: PlainTextNetworking) =
    it should "be able to accept incoming connections from remote endpoints" in:
      println(s"[${System.currentTimeMillis()}] Test 4 started")
      val receivedMessages = CopyOnWriteArrayList[net.MessageIn]()
      val messages = Seq("Hello", "World", "Scafi", "Networking")
      val result = usingServer(msg => receivedMessages.add(msg): Unit):
        _ send messages verifying:
          println(
            s"  [${Thread.currentThread().getName()} @ ${System.currentTimeMillis()}] now is: ${System.currentTimeMillis()}",
          )
          eventually(receivedMessages should contain theSameElementsInOrderAs messages)
      result.onComplete(_ => println(s"[${System.currentTimeMillis()}] Test 4 completed"))
      result

    it should "close connections with clients attempting to flood the server" in:
      println(s"[${System.currentTimeMillis()}] Test 5 started")
      val tooLargeMessage = "A" * 65_536
      val result = usingServer(nop): client =>
        client send Seq(tooLargeMessage) verifying eventually:
          println(
            s"  [${Thread.currentThread().getName()} @ ${System.currentTimeMillis()}] now is: ${System.currentTimeMillis()}",
          )
          client.isOpen shouldBe false
      result.onComplete(_ => println(s"[${System.currentTimeMillis()}] Test 5 completed"))
      result
  end both

  private def usingServer[Result](using
      net: Networking,
  )(onMessage: net.MessageIn => Unit)(todo: net.Connection => Future[Result]) =
    for
      server <- net.in(FreePort)(onMessage).run()
      client <- net.out(Endpoint(Localhost, server.listener.boundPort)).run()
      result <- todo(client).transform: res =>
        println(
          s"  [${Thread.currentThread().getName()} @ ${System.currentTimeMillis()}] closing server and client",
        )
        client.close()
        server.listener.close()
        res
    yield result

  extension (using net: Networking)(client: net.Connection)
    infix def send(messages: Seq[net.MessageOut]) =
      messages.foldLeft(Future.unit)((acc, msg) => acc.flatMap(_ => client.send(msg)))

  private val nop: Any => Unit = _ => ()

end SocketNetworkingBehavior
