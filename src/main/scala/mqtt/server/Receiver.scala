package mqtt.server

import java.io.InputStream

import mqtt.model.ErrorPacket.ChannelClosed
import mqtt.model.Packet
import mqtt.model.Packet.Disconnect
import mqtt.parser.MqttPacketParser
import mqtt.utils.BitImplicits._
import mqtt.utils.VariableLengthInteger
import rx.lang.scala.Observable

import scala.annotation.tailrec
import scala.util.Try

/**
 * Contains the receive logic.
 * Interfaces with clients' input streams.
 */
object Receiver {
  
  private trait Action
  
  private case object Continue extends Action
  
  private case object CloseAndComplete extends Action
  
  private case object AlertCloseAndComplete extends Action
  
  private trait InputStreamIterator extends Iterator[Byte] {
    def read(count: Int): Seq[Byte]
  }
  
  private implicit class RichInputStream(in: InputStream) {
    def toIterator: InputStreamIterator = new InputStreamIterator {
      val buffer = new Array[Byte](1)
      
      override def hasNext: Boolean = true
      
      override def next(): Byte = {
        in.read(buffer)
        buffer(0)
      }
      
      override def read(count: Int): Seq[Byte] = {
        val tmpBuffer = new Array[Byte](count)
        val read = in.read(tmpBuffer)
        tmpBuffer.toSeq.take(read)
      }
    }
  }
  
  /**
   * Receives bits from the specified socket and creates a source of packets (Observer)
   * @param idSocket the socket to listen to
   * @return the source of received packet
   */
  def apply(idSocket: IdSocket): Observable[(IdSocket, Packet)] = Observable[(IdSocket, Packet)](observer => {
    val inputStream = idSocket.socket.getInputStream.toIterator
  
    def close(): Unit = idSocket.socket.close()
  
    def complete(): Unit = observer.onCompleted()
  
    def emitAlert(): Unit = if (!idSocket.socket.isClosed) { //TODO can be false if the socket is closed, can cause problems
      println(Thread.currentThread() + "    Error on socket " + idSocket.id)
      observer.onNext((idSocket, ChannelClosed())) //emit
    }
  
    def emitPacket(packet: Packet): Unit = {
      println(Thread.currentThread() + "    Received: " + packet + " from socket " + idSocket.id)
      observer.onNext((idSocket, packet)) //emit
    }
  
    def nextPacket: Option[Packet] = {
      inputStream.next() match {
        case 0 => Option.empty
        case firstByte: Byte => VariableLengthInteger.decode(inputStream) match {
          case Some(length) =>
            val buffer = inputStream.read(length)
            if (buffer.length == length) {
              val packetData = (firstByte +: VariableLengthInteger.encode(length)) ++ buffer
              Option(MqttPacketParser.parse(packetData.toBitsSeq))
            } else {
              Option.empty
            }
          case None => Option.empty
        }
      }
    }
  
    @tailrec def receive(): Unit = {
      Try[Action] {
        nextPacket match {
          case Some(packet: Disconnect) => emitPacket(packet); CloseAndComplete
          case Some(packet) => emitPacket(packet); Continue
          case None => AlertCloseAndComplete
        }
      } getOrElse[Action] AlertCloseAndComplete match {
        case Continue => receive()
        case CloseAndComplete => close(); complete()
        case AlertCloseAndComplete => emitAlert(); close(); complete()
      }
    }
    
    receive()
  })
}
