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

  case class Person(id: Option[Long],
                    firstName: String,
                    lastName: String,
                    gender: Gender)

  case object PersonNotFoundError
}
