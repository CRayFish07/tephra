package org.lpw.tephra.ctrl.status;

import net.sf.json.JSONObject;
import org.lpw.tephra.bean.BeanFactory;
import org.lpw.tephra.bean.ContextRefreshedListener;
import org.lpw.tephra.util.Logger;
import org.lpw.tephra.util.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author lpw
 */
@Service("tephra.ctrl.status")
public class StatusImpl implements Status, ContextRefreshedListener {
    @Autowired
    protected Validator validator;
    @Autowired
    protected Logger logger;
    @Value("${tephra.ctrl.status.uri:/tephra/ctrl/status}")
    protected String uri;
    protected boolean enable;
    protected JSONObject version;

    @Override
    public boolean isStatus(String uri) {
        return enable && this.uri.equals(uri);
    }

    @Override
    public JSONObject execute(int counter) {
        JSONObject json = new JSONObject();
        json.put("concurrent", counter);
        json.put("timestamp", System.currentTimeMillis());
        json.put("version", version);

        return json;
    }

    @Override
    public int getContextRefreshedSort() {
        return 8;
    }

    @Override
    public void onContextRefreshed() {
        enable = !validator.isEmpty(uri);
        if (logger.isInfoEnable())
            logger.info("设置服务状态启动状态：{}。", enable);

        version();
        if (logger.isDebugEnable())
            logger.debug("设置版本信息：{}。", version);
    }

    protected void version() {
        Set<String> set = new HashSet<>();
        for (String beanName : BeanFactory.getBeanNames()) {
            Object bean = BeanFactory.getBean(beanName);
            if (bean != null)
                set.add(bean.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        }

        Map<String, Set<String>> map = new HashMap<>();
        set.forEach(path -> {
            int[] range = new int[]{path.lastIndexOf('/'), path.lastIndexOf('.')};
            if (range[0] == -1 || range[1] == -1 || range[0] > range[1])
                return;

            path = path.substring(range[0] + 1, range[1]);
            if (path.startsWith("spring"))
                return;

            int indexOf = indexOfNumber(path);
            if (indexOf == -1)
                return;

            putToMap(map, path.substring(0, indexOf - 1), path.substring(indexOf));
        });
        setVersion(map);
    }

    protected int indexOfNumber(String string) {
        for (int i = 0, length = string.length(); i < length; i++) {
            char ch = string.charAt(i);
            if (ch >= '0' && ch <= '9')
                return i;
        }

        return -1;
    }

    protected void putToMap(Map<String, Set<String>> map, String name, String version) {
        StringBuilder sb = new StringBuilder();
        for (String string : name.split("-")) {
            sb.append('-').append(string);
            Set<String> set = map.get(sb.toString());
            if (set == null)
                set = new HashSet<>();
            set.add(version);
            map.put(sb.toString(), set);
        }
    }

    protected void setVersion(Map<String, Set<String>> map) {
        Set<String> set = new HashSet<>();
        map.forEach((key, value) -> {
            if (value.size() > 1) {
                set.add(key);

                return;
            }
        });
        set.forEach(key -> map.remove(key));

        Map<String, String> versions = new HashMap<>();
        map.forEach((key, value) -> versions.put(key, value.iterator().next()));

        set.clear();
        versions.forEach((key, value) -> versions.forEach((k, v) -> {
            if (key.contains(k) && !key.equals(k) && value.equals(v))
                set.add(key);
        }));
        set.forEach(key -> versions.remove(key));

        version = new JSONObject();
        versions.forEach((key, value) -> version.put(key.substring(1), value));
    }
}
