package wow

import wow.api.WebServerConfiguration
import wow.auth.AuthServerConfiguration
import wow.realm.RealmServerConfiguration

/**
  * Application's configuration
  */
case class ApplicationConfiguration(
  webServer: WebServerConfiguration,
  auth: AuthServerConfiguration,
  realms: Map[Int, RealmServerConfiguration]
)

