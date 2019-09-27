package mqtt.model


sealed trait QoS

object QoS {
  
  private case object QoS0 extends QoS
  
  private case object QoS1 extends QoS
  
  private case object QoS2 extends QoS

  def apply(value: Int): QoS = value match {
    case 0 => QoS0
    case 1 => QoS1
    case 2 => QoS2
  }
  
  def unapply(arg: QoS): Option[Int] = Option(arg match {
    case QoS0 => 0
    case QoS1 => 1
    case QoS2 => 2
  })
  
  
  implicit class QoSExtensions(qos: QoS){
    def value:Int = qos match {
      case QoS0 => 0
      case QoS1 => 1
      case QoS2 => 2
    }
  }
}

object Types {
  type Payload = Seq[Byte]
  type Password = Seq[Byte]
  type ClientID = String
  type PackedID = Int
}