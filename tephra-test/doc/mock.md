# 使用Mock测试控制器
1、引入tephra-test依赖，如：
```xml
        <dependency>
            <groupId>org.lpw.tephra</groupId>
            <artifactId>tephra-test</artifactId>
            <version>1.0.0-RELEASE</version>
            <scope>test</scope>
        </dependency>
```
2、假设控制器代码如下：
```java
package org.lpw.hellotephra;
 
import org.lpw.tephra.ctrl.context.Request;
import org.lpw.tephra.ctrl.execute.Execute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
 
import java.util.HashMap;
import java.util.Map;
 
/**
 * @author lpw
 */
@Controller("hellotephra.ctrl")
public class HelloCtrl {
    @Autowired
    protected Request request;
 
    @Execute(name = "/hello")
    public Object hello() {
        return "hello " + request.get("name");
    }
 
    @Execute(name = "/hi", type = "freemarker", template = "hi")
    public Object hi() {
        Map<String, String> map = new HashMap<>();
        map.put("name", request.get("name"));
 
        return map;
    }
}
```
hi.ftl代码如下：
```freemarker
hi ${data.name} !
```
3、则HelloCtrl测试代码参考如下：
```java
package org.lpw.hellotephra;
 
import org.junit.Test;
import org.lpw.tephra.test.TephraTestSupport;
import org.lpw.tephra.test.mock.MockHelper;
import org.lpw.tephra.util.Context;
import org.springframework.beans.factory.annotation.Autowired;
 
import java.io.File;
import java.io.IOException;
 
import static org.junit.Assert.assertEquals;
 
/**
 * @author lpw
 */
public class HelloCtrlTest extends TephraTestSupport {
    @Autowired
    protected Context context;
    @Autowired
    protected MockHelper mockHelper;
 
    @Test
    public void hello() {
        mockHelper.mock("/hello");
        mockHelper.getRequest().addParameter("name", "tephra");
        assertEquals("{\"code\":0,\"data\":\"hello tephra\"}", mockHelper.getResponse().getOutputStream().toString());
    }
 
    @Test
    public void hi() {
        mockHelper.mock("../hello-tephra-web/src/main/webapp", "/hi");
        mockHelper.getRequest().addParameter("name", "tephra");
        assertEquals("hi tephra !", mockHelper.getResponse().getOutputStream().toString());
    }
}
```
4、输出结果：
```log
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```