package wow

import wow.api.WebServerConfiguration
import wow.auth.AuthServerConfiguration
import wow.realm.RealmServerConfiguration

/**
  * Created by sknz on 5/6/17.
  */
case class ApplicationConfiguration(
  webServer: WebServerConfiguration,
  auth: AuthServerConfiguration,
  realms: Map[Int, RealmServerConfiguration]
)

