package org.lpw.tephra.ctrl.validate;

import org.lpw.tephra.ctrl.TrustfulIp;
import org.lpw.tephra.ctrl.context.Header;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;

/**
 * @author lpw
 */
@Controller(Validators.SIGN)
public class SignValidatorImpl extends ValidatorSupport {
    @Inject
    private Header header;
    @Inject
    private TrustfulIp trustfulIp;

    @Override
    public boolean validate(ValidateWrapper validate, String parameter) {
        return trustfulIp.contains(header.getIp()) || request.checkSign();
    }

    @Override
    public int getFailureCode(ValidateWrapper validate) {
        return 9995;
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "illegal-sign";
    }
}
