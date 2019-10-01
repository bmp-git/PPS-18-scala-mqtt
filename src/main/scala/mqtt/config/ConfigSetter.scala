package mqtt.config

import mqtt.model.BrokerConfig

case class ConfigSetter(run: BrokerConfig => Option[BrokerConfig]) {
  def map(f: BrokerConfig => BrokerConfig): ConfigSetter =
    this.flatMap(a => ConfigSetter(_ => Option(f(a))))
  
  def flatMap(f: BrokerConfig => ConfigSetter): ConfigSetter =
    ConfigSetter(firstConfig => this.run(firstConfig) flatMap (a => f(a).run(a)))
}
