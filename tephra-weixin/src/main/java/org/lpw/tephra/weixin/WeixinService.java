package org.lpw.tephra.weixin;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * 微信监听服务。
 *
 * @author lpw
 */
public interface WeixinService {
    /**
     * 微信URI地址。
     */
    String URI = "/tephra/weixin/";
    /**
     * 支付网关是否存在验证器Bean名称。
     */
    String VALIDATOR_EXISTS_PAY_GATEWAY = "tephra.weixin.validator.pay-gateway.exists";

    /**
     * 验证echo信息是否正确。
     *
     * @param appId     微信公众号AppID。
     * @param signature 签名。
     * @param timestamp 时间戳。
     * @param nonce     随机数。
     * @return 如果验证成功则返回true；否则返回false。
     */
    boolean echo(String appId, String signature, String timestamp, String nonce);

    /**
     * 认证微信用户信息。
     *
     * @param appId 微信公众号AppID。
     * @param code  认证码。
     * @return 微信用户信息，如果认证失败则返回空JSON。
     */
    JSONObject auth(String appId, String code);

    /**
     * 处理接收到的XML数据。
     *
     * @param appId 微信公众号AppID。
     * @param xml   XML数据。
     * @return 处理结果。
     */
    String xml(String appId, String xml);

    /**
     * 预充值。
     *
     * @param type    充值类型。
     * @param mpId    微信公众号AppID。
     * @param orderNo 订单号。
     * @param body    订单内容。
     * @param amount  金额，单位：分。
     * @return 支付参数；如果发起失败则返回null。
     */
    String prepay(String type, String mpId, String orderNo, String body, int amount);

    /**
     * 处理充值回调。
     *
     * @param parameters 参数集。
     * @return 结果。
     */
    String pay(Map<String, String> parameters);

    /**
     * 获取当前微信用户的OpenID。
     *
     * @return 当前微信用户的OpenID；如果不存在则返回null。
     */
    String getOpenId();

    /**
     * 获取当前微信用户昵称。
     *
     * @return 当前微信用户昵称；如果不存在则返回null。
     */
    String getNickname();

    /**
     * 获取当前微信用户头像URL地址。
     *
     * @return 当前微信用户头像URL地址；如果不存在则返回null。
     */
    String getPortrait();
}
