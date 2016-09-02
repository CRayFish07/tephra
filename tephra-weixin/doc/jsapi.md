# 使用JSAPI发起支付请求
需要在微信公众号里发起支付请求时，可以通过WeixinHelper获取支付网关：
```java
PayGateway gateway = weixinHelper.getPayGateway(PayGateway.JSAPI);
```
然后调用prepay获取JSAPI支付所需参数。PayGateway接口描述如下：
```java
package org.lpw.tephra.weixin.gateway;

import java.util.Map;

/**
 * 微信支付网关。
 *
 * @author lpw
 */
public interface PayGateway {
    /**
     * 微信公众号支付类型。
     */
    String JSAPI = "JSAPI";

    /**
     * 获取网关类型。
     *
     * @return 网关类型。
     */
    String getType();

    /**
     * 发起预支付请求。
     *
     * @param mpId    微信公众号AppID。
     * @param openId  微信用户OpenID。
     * @param orderNo 订单号。
     * @param body    订单内容。
     * @param amount  金额，单位：分。
     * @return 支付参数；如果发起失败则返回null。
     */
    String prepay(String mpId, String openId, String orderNo, String body, int amount);

    /**
     * 支付回调。
     *
     * @param parameters 参数集。
     * @return 返回结果。
     */
    void callback(Map<String, String> parameters);
}
```
支付完成后会回调给WeixinListener实现。
