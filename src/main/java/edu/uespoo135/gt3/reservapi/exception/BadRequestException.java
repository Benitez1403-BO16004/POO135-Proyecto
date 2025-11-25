package edu.uespoo135.gt3.reservapi.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}