package person.model

/**
  * @author Ayush Mittal
  */
package object model {

  abstract sealed class Gender(val value: String)
  case object Male extends Gender("male")
  case object Female extends Gender("female")
  case object Other extends Gender("other")
  case object NotSpecified extends Gender("not specified")

  object Gender {
    private def values = Set(Male, Female, Other, NotSpecified)

    def unsafeFromString(value: String): Gender = {
      values.find(_.value == value).get
    }
  }

  case class Person(id: Option[Long] = None,
                    firstName: String,
                    lastName: String,
                    gender: Gender)

  case class PersonTwitterInfo(id: Option[Long] = None,
                               screenName: String,
                               userId: Long)

  case class PersonRequest(firstName: String,
                           lastName: String,
                           gender: Gender,
                           screenName: String)

  case class PersonResponse(id: Long,
                            firstName: String,
                            lastName: String,
                            gender: Gender,
                            screenName: String)

  object PersonResponse {
    def fromPersonData(personData: PersonData) =
      PersonResponse(
        personData.id,
        personData.personRequest.firstName,
        personData.personRequest.lastName,
        personData.personRequest.gender,
        personData.personRequest.screenName
      )
  }

  type Tweet = String

  case class PersonData(id: Long, personRequest: PersonRequest)

  object PersonData {
    def fromPerson(person: Person,
                   personTwitterInfo: PersonTwitterInfo): PersonData =
      PersonData(person.id.get,
                 PersonRequest(person.firstName,
                               person.lastName,
                               person.gender,
                               personTwitterInfo.screenName))
  }

  case class PersonTweets(screenName: String, tweets: List[Tweet])

  case object NotFoundError
}
