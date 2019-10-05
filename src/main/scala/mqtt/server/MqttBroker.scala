package mqtt.server

import java.net.{InetAddress, ServerSocket}
import java.util.concurrent.Executors

import com.typesafe.scalalogging.LazyLogging
import mqtt.model.{BrokerConfig, Packet}
import rx.lang.scala.Observable
import rx.lang.scala.schedulers.{ExecutionContextScheduler, IOScheduler}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * MQTT broker for version 3.1.1.
 *
 * @param brokerConfig broker configurations
 * @param usersConfig  users configurations
 */
case class MqttBroker(brokerConfig: BrokerConfig, usersConfig: Map[String, Option[String]]) extends LazyLogging {
  
  private val MAXIMUM_INCOMING_CONNECTION = 50
  
  /**
   * Socket listener.
   */
  private lazy val server = brokerConfig.bindAddress match {
    case Some(bindAddress) => new ServerSocket(brokerConfig.port, MAXIMUM_INCOMING_CONNECTION, InetAddress.getByName(bindAddress))
    case None => new ServerSocket(brokerConfig.port, MAXIMUM_INCOMING_CONNECTION)
  }
  
  /**
   * Scheduler used for io.
   */
  private lazy val ioScheduler = IOScheduler()
  /**
   * Executor for handling packets.
   */
  private lazy val packetHandlerExecutor = Executors.newSingleThreadExecutor()
  /**
   * Scheduler used for handling packets. (single threaded).
   */
  private lazy val packetHandlerScheduler = ExecutionContextScheduler(ExecutionContext.fromExecutor(packetHandlerExecutor))
  /**
   * Executor for run the main stream.
   */
  private lazy val mainExecutor = Executors.newSingleThreadExecutor()
  /**
   * Scheduler used for run the main stream.
   */
  private lazy val mainScheduler = ExecutionContextScheduler(ExecutionContext.fromExecutor(mainExecutor))
  /**
   * State of the program.
   */
  private lazy val program = ProgramState(brokerConfig, usersConfig)
  
  //Utilities functions
  private val mapSocket: IdSocket => Unit = idSocket => program.addSocket(idSocket)
  private val unmapSocket: Int => Unit = id => program.removeSocket(id)
  private val socketId: ((Int, Packet)) => Int = _._1
  private val packet: ((Int, Packet)) => Packet = _._2
  private val closePacket: Packet => Boolean = {
    case `ClosePacket` => true
    case _ => false
  }
  
  /**
   * The stream of incoming clients.
   */
  private lazy val incomingClients = Listener(server).subscribeOn(mainScheduler)
  
  /**
   * Every 1 second it generates an empty Option. Needed to check timeouts.
   */
  private lazy val tickStream = Observable.interval(1 second).takeUntil(_ => server.isClosed).map(_ => Option.empty).subscribeOn(ioScheduler)
  /**
   * Generates a stream of packets of a client.
   */
  private val clientReceiver = (client: IdSocket) => Receiver(client).map(Option.apply).subscribeOn(ioScheduler)
    .onBackpressureDrop(p => logger.info(s"Drop packet: $p"))
  
  /**
   * Generates a stream of packets from an input packet.
   * Ex: receive a publish, emit 3 publish
   */
  private val packetHandler = (packet: Option[(IdSocket, Packet)]) =>
    ProtocolHandler(packet, program).subscribeOn(packetHandlerScheduler)
  /**
   * Send al packets to the specified client.
   * When complete generate a value: the id of the closed socket.
   */
  private val clientSender: ((Int, Observable[(Int, Packet)])) => Observable[Int] =
    grouped => Observable[Int](s => grouped match {
      case (socketId, toSend) =>
        toSend.map(packet).takeUntil(closePacket)
          .doOnCompleted {
            s.onNext(socketId)
            s.onCompleted()
          }.subscribeOn(ioScheduler)
          .subscribe(Sender(program.socket(socketId)))
    })
  
  /**
   * Can be used for stop the server.
   */
  trait Breaker {
    /**
     * Stops the server.
     */
    def stop(): Unit
  }
  
  /**
   * THe default breaker.
   */
  private val breaker: Breaker = () => {
    server.close()
    mainExecutor.shutdown()
    packetHandlerExecutor.shutdown()
  }
  
  /**
   * Runs the mqtt broker.
   *
   * @return a breaker capable of stop the broker
   */
  def run(): Breaker = {
    incomingClients
      .doOnEach(mapSocket)
      .flatMap(clientReceiver)
      .merge(tickStream)
      .flatMap(packetHandler)
      .groupBy(socketId)
      .flatMap(clientSender)
      .subscribe(unmapSocket, _ => logger.info("Closing main stream."))
  
    breaker
  }
}
