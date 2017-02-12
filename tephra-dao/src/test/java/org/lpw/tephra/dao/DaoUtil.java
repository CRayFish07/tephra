package org.lpw.tephra.dao;

import com.alibaba.fastjson.JSONObject;
import org.lpw.tephra.bean.BeanFactory;
import org.lpw.tephra.dao.jdbc.DataSource;

/**
 * @author lpw
 */
public class DaoUtil {
    public static void createDataSource(String key) {
        createDataSource(key, new String[]{"127.0.0.1:3306", "localhost:3306"});
    }

    public static void createDataSource(String key, String[] ips) {
        JSONObject config = new JSONObject();
        config.put("key", key);
        config.put("type", "mysql");
        config.put("username", "root");
        config.put("password", "root");
        config.put("ips", ips);
        config.put("schema", "d_tephra_test");
        BeanFactory.getBean(DataSource.class).create(config);
    }

    public static void createSessionFactory(String key, ConnectionFactory<?> sessionFactory) {
        createDataSource(key);

        JSONObject config = new JSONObject();
        config.put("key", key);
        config.put("values", new String[]{"org.lpw.tephra"});
        sessionFactory.create(config);
    }
}
