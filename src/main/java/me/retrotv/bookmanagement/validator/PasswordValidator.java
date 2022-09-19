package me.retrotv.bookmanagement.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        final int MIN = 8;
        final int MAX = 20;

        // 영어, 숫자, 특수문자 포함한 MIN to MAX 글자 정규식
        final String REGEX = "^((?=.*\\d)(?=.*[a-zA-Z])(?=.*[\\W]).{" + MIN + "," + MAX + "})$";

        // 3자리 연속 문자 정규식
        final String SAMEPT = "(\\w)\\1\\1";

        // 공백 문자 정규식
        final String BLANKPT = "(\\s)";

        Matcher matcher;

        // 공백 체크
        if(value == null || "".equals(value)) {
            return false;
        }
    
        String tmpPw = value.toUpperCase();
        int strLen = tmpPw.length();
    
        if(strLen > 20 || strLen < 8) {
            return false;
        }
    
        matcher = Pattern.compile(BLANKPT).matcher(tmpPw);
        if(matcher.find()) {
            return false;
        }
    
        matcher = Pattern.compile(SAMEPT).matcher(tmpPw);
        if(matcher.find()) {
            return false;
        }

        matcher = Pattern.compile(REGEX).matcher(tmpPw);
        if(!matcher.find()) {
            return false;
        }

        return true;
    }
}
