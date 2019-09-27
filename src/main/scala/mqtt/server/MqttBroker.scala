package mqtt.server

import java.net.ServerSocket
import java.util.concurrent.Executors

import mqtt.model.Packet
import rx.lang.scala.Observable
import rx.lang.scala.schedulers.{ExecutionContextScheduler, IOScheduler}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * MQTT broker for version 3.1.1.
 *
 * @param port the listen port
 */
case class MqttBroker(port: Int) {
  /**
   * Socket listener.
   */
  private val server = new ServerSocket(port)
  /**
   * Scheduler used for io.
   */
  private val ioScheduler = IOScheduler()
  /**
   * Executor for handling packets.
   */
  private val packetHandlerExecutor = Executors.newSingleThreadExecutor()
  /**
   * Scheduler used for handling packets. (single threaded).
   */
  private val packetHandlerScheduler = ExecutionContextScheduler(ExecutionContext.fromExecutor(packetHandlerExecutor))
  /**
   * Executor for run the main stream.
   */
  val mainExecutor = Executors.newSingleThreadExecutor()
  /**
   * Scheduler used for run the main stream.
   */
  val mainScheduler = ExecutionContextScheduler(ExecutionContext.fromExecutor(mainExecutor))
  /**
   * State of the program.
   */
  private val program = ProgramState()
  
  //Utilities functions
  private val mapSocket: IdSocket => Unit = idSocket => program.addSocket(idSocket)
  private val unmapSocket: Int => Unit = id => program.removeSocket(id)
  private val socketId: ((Int, Packet)) => Int = _._1
  private val packet: ((Int, Packet)) => Packet = _._2
  private val isClosePacket: Packet => Boolean = {
    case `ClosePacket` => true
    case _ => false
  }
  
  /**
   * Every 1 second it generates an empty Option.
   */
  private val tickStream = Observable.interval(1 second).takeUntil(_ => server.isClosed).map(_ => Option.empty).subscribeOn(ioScheduler)
  /**
   * Generates a stream of packets of a client.
   */
  private val clientReceiver = (client: IdSocket) => Receiver(client).subscribeOn(ioScheduler)
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
        toSend.map(packet).takeUntil(isClosePacket)
          .doOnCompleted {
            s.onNext(socketId)
            s.onCompleted()
          }.subscribeOn(ioScheduler)
          .subscribe(Sender(program.socket(socketId)))
    })
  
  /**
   * Can be used for stop the server.
   */
  trait Stopper {
    /**
     * Stop the server.
     */
    def stop(): Unit
  }
  
  /**
   * Run the mqtt broker.
   * Non blocking.
   */
  def run(): Stopper = {
    Listener(server).subscribeOn(mainScheduler)
      .doOnEach(mapSocket)
      .flatMap(clientReceiver)
      .map(Option.apply)
      .merge(tickStream)
      .flatMap(packetHandler)
      .groupBy(socketId)
      .flatMap(clientSender)
      .subscribe(unmapSocket, _ => println("Exception on main stream."))
    
    () => {
      server.close()
      mainExecutor.shutdown()
      packetHandlerExecutor.shutdown()
    }
  }
}
