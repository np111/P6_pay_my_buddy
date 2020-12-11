package com.paymybuddy.api.util.validation.constraint;

import com.paymybuddy.api.model.Currency;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import lombok.SneakyThrows;

@Constraint(validatedBy = IsAmount.Validator.class)
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR,
        ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(IsAmount.List.class)
public @interface IsAmount {
    String amountField();

    String currencyField();

    boolean strictlyPositive() default false;

    String message() default "{invalid.amount}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR,
            ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        IsAmount[] value();
    }

    class Validator implements ConstraintValidator<IsAmount, Object> {
        private String amountField;
        private String currencyField;
        private boolean strictlyPositive;

        @Override
        public void initialize(IsAmount constraintAnnotation) {
            amountField = constraintAnnotation.amountField();
            currencyField = constraintAnnotation.currencyField();
            strictlyPositive = constraintAnnotation.strictlyPositive();
        }

        @SneakyThrows
        @Override
        public boolean isValid(Object holder, ConstraintValidatorContext ctx) {
            BigDecimal amount = (BigDecimal) new PropertyDescriptor(amountField, holder.getClass()).getReadMethod().invoke(holder);
            if (amount == null) {
                return true;
            }
            if (strictlyPositive && amount.compareTo(BigDecimal.ZERO) <= 0) {
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate("must be strictly positive").addConstraintViolation();
                return false;
            }
            Currency currency = (Currency) new PropertyDescriptor(currencyField, holder.getClass()).getReadMethod().invoke(holder);
            if (currency != null && amount.scale() > currency.getDecimals()) {
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate("is limited to " + currency.getDecimals() + " decimal places").addConstraintViolation();
                return false;
            }
            return true;
        }
    }
}
