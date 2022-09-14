package me.retrotv.bookmanagement.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ISBNValidator implements ConstraintValidator<ISBN, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null) {
            return true;
        }

        if(value.length() != 0 && value.length() != 13) {
            return false;
        }
        
        return value.matches("\\d{13}") || value.isEmpty();
    }
}
