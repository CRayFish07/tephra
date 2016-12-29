package org.lpw.tephra.bean;

import org.lpw.tephra.scheduler.MinuteJob;
import org.lpw.tephra.util.Context;
import org.lpw.tephra.util.Converter;
import org.lpw.tephra.util.Generator;
import org.lpw.tephra.util.Io;
import org.lpw.tephra.util.Logger;
import org.lpw.tephra.util.Validator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lpw
 */
@Component("tephra.bean.class-reloader")
public class ClassReloaderImpl implements ClassReloader, MinuteJob, ApplicationContextAware {
    @Inject
    private Converter converter;
    @Inject
    private Context context;
    @Inject
    private Io io;
    @Inject
    private Validator validator;
    @Inject
    private Generator generator;
    @Inject
    private Logger logger;
    @Inject
    private Container container;
    @Value("${tephra.bean.reload.class-path:}")
    private String classPath;
    private List<ClassLoader> loaders;
    private Set<String> names;
    private Map<Class<?>, List<Autowire>> autowires;
    private ApplicationContext applicationContext;
    private long lastModified = 0L;

    @Override
    public boolean isReloadEnable(String name) {
        return names.contains(name);
    }

    @Override
    public String getClassPath() {
        return context.getAbsolutePath(classPath);
    }

    @Override
    public void executeMinuteJob() {
        if (validator.isEmpty(classPath))
            return;

        names = names();
        if (names.isEmpty())
            return;

        if (logger.isInfoEnable())
            logger.info("重新载入类：{}", names);

        if (loaders == null) {
            loaders = new ArrayList<>();
            loaders.add(applicationContext.getClassLoader());
        }

        if (autowires == null) {
            autowires = new ConcurrentHashMap<>();

            for (String name : container.getBeanNames())
                autowire(container.getBeanClass(name), name, null);
        }

        ClassLoader loader = new DynamicClassLoader(loaders.get(loaders.size() - 1));
        names.forEach((name) -> load(loader, name));
        loaders.add(loader);
    }

    private Set<String> names() {
        Set<String> set = new HashSet<>();
        File file = new File(context.getAbsolutePath(classPath + "/name"));
        if (file.lastModified() <= lastModified)
            return set;

        lastModified = file.lastModified();
        String path = file.getAbsolutePath();
        String names = new String(io.read(path)).trim();
        if (validator.isEmpty(names))
            return set;

        for (String name : converter.toArray(names, "\n"))
            if (name.trim().length() > 0)
                set.add(name.trim());

        io.write(path, new byte[0]);

        return set;
    }

    private void autowire(Class<?> beanClass, String beanName, Object bean) {
        for (Field field : beanClass.getDeclaredFields()) {
            if (field.getAnnotation(Inject.class) == null)
                continue;

            try {
                field.setAccessible(true);
                Class<?> key = field.getType();
                boolean collection = isCollection(key);
                if (collection) {
                    Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    if (type instanceof Class)
                        key = (Class<?>) type;
                    else
                        continue;
                }

                List<Autowire> list = autowires.get(key);
                if (list == null)
                    list = new ArrayList<>();
                list.add(new Autowire(bean == null ? container.getBean(beanName) : bean, field, collection));
                autowires.put(key, list);
            } catch (Exception e) {
                logger.warn(e, "解析[{}]属性[{}]依赖时发生异常！", beanClass, field.getName());
            }
        }
    }

    private boolean isCollection(Class<?> clazz) {
        try {
            return clazz.equals(clazz.asSubclass(Collection.class));
        } catch (Exception e) {
            return false;
        }
    }

    private void load(ClassLoader loader, String name) {
        try {
            DefaultListableBeanFactory lbf = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
            BeanDefinition bd = BeanDefinitionReaderUtils.createBeanDefinition(null, name, loader);
            String dynamicBeanName = generator.uuid();
            lbf.registerBeanDefinition(dynamicBeanName, bd);
            Object bean = lbf.getBean(dynamicBeanName);
            String beanName = getBeanName(bean.getClass());
            Object oldBean = null;
            if (beanName != null) {
                oldBean = container.getBean(beanName);
                container.mapBeanName(beanName, dynamicBeanName);
            }
            autowire(bean.getClass(), null, bean);
            autowired(bean, oldBean);
        } catch (Exception e) {
            logger.warn(e, "重新载入[{}]时发生异常！", name);
        }
    }

    private String getBeanName(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        if (component != null)
            return component.value();

        Repository repository = clazz.getAnnotation(Repository.class);
        if (repository != null)
            return repository.value();

        Service service = clazz.getAnnotation(Service.class);
        if (service != null)
            return service.value();

        Controller controller = clazz.getAnnotation(Controller.class);
        if (controller != null)
            return controller.value();

        return null;
    }

    @SuppressWarnings("unchecked")
    private void autowired(Object bean, Object oldBean) throws IllegalArgumentException, IllegalAccessException {
        for (Class<?> key : autowires.keySet()) {
            if (!key.isInstance(bean))
                continue;

            for (Autowire autowire : autowires.get(key)) {
                Object value = bean;
                if (autowire.isCollection()) {
                    Collection<Object> collection = (Collection<Object>) autowire.getField().get(autowire.getBean());
                    if (oldBean != null)
                        collection.remove(oldBean);
                    collection.add(bean);
                    value = collection;
                }
                autowire.getField().set(autowire.getBean(), value);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
