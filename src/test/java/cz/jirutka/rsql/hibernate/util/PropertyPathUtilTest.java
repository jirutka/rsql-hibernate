package cz.jirutka.rsql.hibernate.util;

import cz.jirutka.rsql.hibernate.entity.Department;
import cz.jirutka.rsql.hibernate.entity.Person;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit Tests for {@link cz.jirutka.rsql.hibernate.util.PropertyPathUtil}
 *
 * @author Andrej Anafinow <javix@gmx.de>
 *         Date 01.04.2014
 */
public class PropertyPathUtilTest {

    @Test
    public void testGetPropertyClass() throws Exception {
        Class<?> type = PropertyPathUtil.getPropertyClass(Department.class, "head");
        Assert.assertNotNull(type);
        Assert.assertEquals(Person.class, type);
    }

    @Test
    public void testGetPropertyClass_NO_PROPERTY() throws Exception {
        Class<?> type = PropertyPathUtil.getPropertyClass(Person.class, "age");
        Assert.assertNull(type);
    }
}
