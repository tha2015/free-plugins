package org.freejava.tools.handlers;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;

public class IndexedPropertyObservableValue extends AbstractObservableValue {

    private Object bean;
    private String property;
    private int index;
    private static final PropertyUtilsBean util = new PropertyUtilsBean();

    public IndexedPropertyObservableValue(Object bean, String property,
            int index) {
        super();
        this.bean = bean;
        this.property = property;
        this.index = index;
    }

    public Object getValueType() {
        Object valueType = null;
        try {
            Class type = util.getPropertyType(bean, property);
            valueType = type.getComponentType();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return valueType;
    }

    @Override
    protected Object doGetValue() {
        Object value = null;
        try {
            value = util.getIndexedProperty(bean, property, index);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return value;
    }

    @Override
    protected void doSetValue(Object value) {
        try {
            util.setIndexedProperty(bean, property, index, value);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
