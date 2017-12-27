package wow.auth.data

import java.sql.SQLException

import org.scalatest._
import wow.auth.crypto.{Srp6Identity, Srp6Protocol}
import wow.common.database.AutoRollbackAfterSuite

/**
  * Tests the Account Dao
  */
@DoNotDiscover
class AccountDaoTest extends FlatSpec with Matchers with AutoRollbackAfterSuite {
  val accountName = "t"
  val accountNameStored: String = accountName.toUpperCase()

  val identity: Srp6Identity = Srp6Identity(
    salt = BigInt("34250773559025062325707019132568240066555050302187666794913620370046688907353"),
    verifier = BigInt("112096801862330810634124091884396918324945651101959033326605231301945343995891")
  )

  private var accountId = -1

  behavior of "Account"

  it should "create a new account" in {
    accountId = Account.create(accountNameStored, identity)(session)
    accountId should be >= 0
  }

  private var expectedAccount: Account = _

  it should "find the created account by name" in {
    val maybeAcc = Account.findByLogin(accountNameStored)

    assert(maybeAcc.isDefined)

    val acc = maybeAcc.get

    assert(acc.id == accountId)
    assert(acc.login == accountNameStored)
    assert(acc.identity == identity)
    assert(acc.sessionKey.isEmpty)

    expectedAccount = Account(accountId, accountNameStored, identity, None)

    assert(acc === expectedAccount)
  }

  it should "find the created account by id" in {
    val maybeAcc = Account.findById(accountId)

    assert(maybeAcc.isDefined)

    val acc = maybeAcc.get

    assert(acc === expectedAccount)
  }

  it should "save changes to the account" in {
    val newIdentity = new Srp6Protocol().computeSaltAndVerifier(accountNameStored, "tt")
    val expectedChangedAccount = expectedAccount.copy(identity = newIdentity,
      sessionKey = Some(BigInt(
        "1598849554174257009559374228067872849457870653498579128752820969641303727240527121547937498899945"))
    )

    // Account login is changed but the change should not be saved to database
    val savedChangedAccount = expectedChangedAccount.copy(login = "other")

    Account.save(savedChangedAccount)

    val maybeChangedAccount = Account.findById(accountId)

    assert(maybeChangedAccount.isDefined)

    val changedAccount = maybeChangedAccount.get

    assert(changedAccount === expectedChangedAccount)
  }

  it should "enforce name uniqueness" in {
    assertThrows[SQLException](Account.create(accountName, identity))
  }
}

