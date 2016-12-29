package org.lpw.tephra.weixin;

import org.lpw.tephra.ctrl.Forward;
import org.lpw.tephra.ctrl.context.Request;
import org.lpw.tephra.ctrl.execute.Execute;
import org.lpw.tephra.ctrl.template.Templates;
import org.lpw.tephra.util.Validator;
import org.lpw.tephra.util.Xml;
import org.lpw.tephra.weixin.gateway.PayGateway;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;

/**
 * @author lpw
 */
@Controller("tephra.weixin.ctrl")
@Execute(name = WeixinService.URI)
public class WeixinCtrl {
    @Inject
    private Validator validator;
    @Inject
    private Xml xml;
    @Inject
    private Request request;
    @Inject
    private Forward forward;
    @Inject
    private WeixinHelper weixinHelper;
    @Inject
    private WeixinService weixinService;

    @Execute(name = "wx.+", type = Templates.STRING)
    public Object service() {
        String uri = request.getUri();
        String appId = uri.substring(uri.lastIndexOf('/') + 1);
        String echo = request.get("echostr");
        if (!validator.isEmpty(echo))
            return weixinService.echo(appId, request.get("signature"), request.get("timestamp"), request.get("nonce")) ? echo : "failure";

        String redirect = request.get("redirect");
        if (!validator.isEmpty(redirect)) {
            weixinService.redirect(appId, request.get("code"));
            forward.redirectTo(redirect);

            return null;
        }

        return weixinService.xml(appId, request.getFromInputStream());
    }

    @Execute(name = "jsapi", type = Templates.STRING)
    public Object jsapi() {
        return callback(PayGateway.JSAPI);
    }

    private String callback(String type) {
        weixinHelper.getPayGateway(type).callback(xml.toMap(request.getFromInputStream(), false));

        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }
}
