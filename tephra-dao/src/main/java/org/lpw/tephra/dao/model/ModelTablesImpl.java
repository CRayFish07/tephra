package org.lpw.tephra.dao.model;

import org.lpw.tephra.bean.BeanFactory;
import org.lpw.tephra.bean.ContextRefreshedListener;
import org.lpw.tephra.util.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lpw
 */
@Repository("tephra.model.tables")
public class ModelTablesImpl implements ModelTables, ContextRefreshedListener {
    @Autowired
    protected Validator validator;
    @Autowired(required = false)
    protected Set<Model> models;
    protected Map<Class<? extends Model>, ModelTable> map;

    @Override
    public ModelTable get(Class<? extends Model> modelClass) {
        ModelTable modelTable = map.get(modelClass);
        if (modelTable == null)
            throw new NullPointerException("无法获得Model类[" + modelClass + "]对应的ModelTable实例！");

        return modelTable;
    }

    @Override
    public Set<Class<? extends Model>> getModelClasses() {
        return new HashSet<>(map.keySet());
    }

    @Override
    public int getContextRefreshedSort() {
        return 2;
    }

    @Override
    public void onContextRefreshed() {
        if (map != null)
            return;

        map = new ConcurrentHashMap<>();
        if (!validator.isEmpty(models))
            for (Model model : models)
                parse(model.getClass());
    }

    protected void parse(Class<? extends Model> modelClass) {
        Table table = modelClass.getAnnotation(Table.class);
        if (table == null)
            return;

        ModelTable modelTable = BeanFactory.getBean(ModelTable.class);
        modelTable.setModelClass(modelClass);
        modelTable.setTableName(table.name());

        Memory memory = modelClass.getAnnotation(Memory.class);
        if (memory != null)
            modelTable.setMemoryName(memory.name());

        Method[] methods = modelClass.getMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.length() < 3)
                continue;

            if (name.equals("getId")) {
                modelTable.setIdColumnName(method.getAnnotation(Column.class).name());

                continue;
            }

            String propertyName = name.substring(3);
            if (name.startsWith("get")) {
                modelTable.addGetMethod(propertyName, method);
                Column column = method.getAnnotation(Column.class);
                if (column != null)
                    modelTable.addColumn(column.name(), propertyName);

                continue;
            }

            if (name.startsWith("set")) {
                modelTable.addSetMethod(propertyName, method);

                continue;
            }
        }

        map.put(modelClass, modelTable);
    }
}
