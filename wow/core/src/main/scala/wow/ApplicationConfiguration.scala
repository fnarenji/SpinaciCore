package wow

import wow.auth.AuthServerConfiguration
import wow.realm.RealmServerConfiguration

/**
  * Application's configuration
  */
case class ApplicationConfiguration(
  auth: AuthServerConfiguration,
  realms: Map[Int, RealmServerConfiguration]
)

