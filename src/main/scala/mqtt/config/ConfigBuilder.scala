package mqtt.config

import mqtt.model.BrokerConfig

/**
 * Keep a function that manipulate an existing BrokerConfig.
 * Implements map and flatMap to enable monadic usage.
 *
 * @param run manipulate an existing BrokerConfig and return a new one or None in case of error
 */
case class ConfigBuilder(run: BrokerConfig => Option[BrokerConfig]) {
  def map(f: BrokerConfig => BrokerConfig): ConfigBuilder =
    flatMap(a => ConfigBuilder(_ => Option(f(a))))
  
  def flatMap(f: BrokerConfig => ConfigBuilder): ConfigBuilder =
    ConfigBuilder(firstConfig => run(firstConfig) flatMap (a => f(a).run(a)))
}
