package wow.realm.objects

import wow.realm.RealmContextData

package object characters {
  /**
    * Get character DAO
    *
    * @param realm current realm
    * @return character DAO
    */
  def CharacterDao(implicit realm: RealmContextData): CharacterDao = realm.characterDao
}
