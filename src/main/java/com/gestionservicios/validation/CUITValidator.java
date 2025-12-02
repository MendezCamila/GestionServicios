package com.gestionservicios.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CUITValidator implements ConstraintValidator<CUIT, String> {

    @Override
    public void initialize(CUIT constraintAnnotation) { }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        String digits = value.replaceAll("\\D", "");
        if (digits.length() != 11) return false;

        int[] weights = {5,4,3,2,7,6,5,4,3,2};
        int sum = 0;
        try {
            for (int i = 0; i < 10; i++) {
                int d = Character.getNumericValue(digits.charAt(i));
                sum += d * weights[i];
            }
            int mod = sum % 11;
            int check = 11 - mod;
            if (check == 11) check = 0;
            else if (check == 10) check = 9;
            int last = Character.getNumericValue(digits.charAt(10));
            return check == last;
        } catch (Exception e) {
            return false;
        }
    }
}
