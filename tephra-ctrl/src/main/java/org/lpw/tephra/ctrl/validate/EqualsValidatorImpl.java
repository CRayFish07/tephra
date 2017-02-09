package org.lpw.tephra.ctrl.validate;

import org.springframework.stereotype.Controller;

/**
 * @author lpw
 */
@Controller(Validators.EQUALS)
public class EqualsValidatorImpl extends ValidatorSupport {
    private static final String DEFAULT_FAILURE_MESSAGE_KEY = Validators.PREFIX + "not-equals";

    @Override
    public boolean validate(ValidateWrapper validate, String[] parameters) {
        if (parameters[0] == null)
            return parameters[1] == null;

        return parameters[0].equals(parameters[1]);
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return DEFAULT_FAILURE_MESSAGE_KEY;
    }
}
