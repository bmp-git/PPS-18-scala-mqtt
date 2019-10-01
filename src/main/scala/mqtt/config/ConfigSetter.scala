package mqtt.config

import mqtt.model.BrokerConfig

/**
 * Keep a function that manipulate an existing BrokerConfig.
 * Implements map and flatMap to enable monadic usage.
 *
 * @param run manipulate an existing BrokerConfig and return a new one or None in case of error
 */
case class ConfigSetter(run: BrokerConfig => Option[BrokerConfig]) {
  def map(f: BrokerConfig => BrokerConfig): ConfigSetter =
    this.flatMap(a => ConfigSetter(_ => Option(f(a))))
  
  def flatMap(f: BrokerConfig => ConfigSetter): ConfigSetter =
    ConfigSetter(firstConfig => run(firstConfig) flatMap (a => f(a).run(a)))
}
