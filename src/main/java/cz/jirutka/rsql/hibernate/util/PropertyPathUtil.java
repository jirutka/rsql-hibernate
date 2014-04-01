package cz.jirutka.rsql.hibernate.util;

import org.hibernate.HibernateException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/**
 * Util class with helper methods around the property path.
 *
 * @author Andrej Anafinow <javix@gmx.de>
 *         Date 01.04.2014
 */
public final class PropertyPathUtil {
    private PropertyPathUtil() {}

    /**
     * Get class of a property
     * @param type owner of the property
     * @param property name of the property
     * @return type of the property or null if the type does not contains the property
     */
    public static Class<?> getPropertyClass(Class<?> type, String property) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(type);
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                Class propertyType = pd.getPropertyType();
                if(pd.getName().equals(property)) {
                    return propertyType;
                }
            }
            return null;
        } catch (IntrospectionException e) {
            throw new HibernateException(e);
        }
    }

}
