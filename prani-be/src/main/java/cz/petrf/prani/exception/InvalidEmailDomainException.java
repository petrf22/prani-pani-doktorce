package cz.petrf.prani.exception;

public class InvalidEmailDomainException extends EmailException {

  public InvalidEmailDomainException(String message) {
    super(message);
  }
}
