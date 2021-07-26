package validator

trait Validator {
  def validate(objectToValidate: Any): Boolean
}
