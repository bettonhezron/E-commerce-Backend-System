package com.hezron.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //Handle email already exists error
    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public  String handleEmailAlreadyExistsException(EmailAlreadyExistsException ex){
       return ex.getMessage();
    }

    //Handle user not found
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException(ResourceNotFoundException ex){
        return ex.getMessage();
    }

    //Handle invalid login credentials
    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleInvalidCredentialsException(InvalidCredentialsException ex){
        return  ex.getMessage();
    }

    //
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGlobalException(Exception ex){
        return "An unexpected error occurred: " + ex.getMessage();
    }


}
