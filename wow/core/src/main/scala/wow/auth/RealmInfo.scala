package wow.auth

import wow.auth.protocol.RealmFlags
import wow.auth.protocol.packets.ServerRealmlistEntry
import wow.realm.RealmServerConfiguration

/**
  * Information about realm (from the point of view of the authserver)
  */
case class RealmInfo(
  id: Int,
  realmConfig: RealmServerConfiguration,
  var flags: RealmFlags.ValueSet = RealmFlags.ValueSet(RealmFlags.Offline),
  private var _population : Float = 0.0f
) {
  require(id > 0)

  def population: Float = _population

  def population_=(newPopulation: Float): Unit = {
    require(newPopulation >= 0)

    _population = Math.min(1.0f, newPopulation)

    if (population >= 0.90f) {
      flags = flags + RealmFlags.Full
    } else {
      flags = flags - RealmFlags.Full
    }
  }

  /**
    * Get the ServerRealmlist packet entry for this realm
    * @param charactersCount number of characters
    * @return ServerRealmlistEntry for this realm
    */
  def toEntry(charactersCount: Int): ServerRealmlistEntry = ServerRealmlistEntry(realmConfig.tpe,
    lock = realmConfig.lock,
    realmConfig.flags ++ flags,
    realmConfig.name,
    s"${realmConfig.host}:${realmConfig.port}",
    // from experiment in client: 0.9 shows as low, 1.0 as medium, 1.1 as high, thus:
    0.8f + Math.ceil(_population * 3).toFloat * 0.1f,
    charactersCount,
    realmConfig.timeZone,
    id)
}

