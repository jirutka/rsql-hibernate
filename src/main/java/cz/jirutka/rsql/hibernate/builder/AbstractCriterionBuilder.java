/*
 * The MIT License
 *
 * Copyright 2013 Jakub Jirutka <jakub@jirutka.cz>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cz.jirutka.rsql.hibernate.builder;

import cz.jirutka.rsql.hibernate.exception.ArgumentFormatException;
import cz.jirutka.rsql.hibernate.exception.UnknownSelectorException;
import cz.jirutka.rsql.parser.model.Comparison;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass of all Criterion Builders.
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public abstract class AbstractCriterionBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCriterionBuilder.class);

    public static final Character LIKE_WILDCARD = '*';
    public static final String NULL_ARGUMENT = "NULL";
    
    
    
    ///////////////  ABSTRACT METHODS  ///////////////
    
    /**
     * This method is called by Criteria Builder to determine if this builder 
     * can handle given comparison (constraint).
     * 
     * @param property property name or path
     * @param entityClass Class of entity that holds given property.
     * @param parent Reference to the parent <tt>CriteriaBuilder</tt>.
     * @return <tt>true</tt> if this builder can handle given property of entity
     *         class, otherwise <tt>false</tt>
     */
    public abstract boolean accept(String property, Class<?> entityClass, CriteriaBuilder parent);
    
    /**
     * Create <tt>Criterion</tt> for given comparison (constraint).
     * 
     * @param property property name or path
     * @param operator comparison operator
     * @param argument argument
     * @param entityClass Class of entity that holds given property.
     * @param alias Association alias (incl. dot) which must be used to prefix 
     *        property name!
     * @param parent Reference to the parent <tt>CriteriaBuilder</tt>.
     * @return Criterion
     * @throws cz.jirutka.rsql.hibernate.exception.ArgumentFormatException If given argument is not in suitable
     *         format required by entity's property, i.e. cannot be cast to 
     *         property's type.
     * @throws cz.jirutka.rsql.hibernate.exception.UnknownSelectorException If such property does not exist.
     */
    public abstract Criterion createCriterion(String property, Comparison operator, String argument, 
            Class<?> entityClass, String alias, CriteriaBuilder parent) 
            throws ArgumentFormatException, UnknownSelectorException;
    
    
    
    
    ///////////////  TEMPLATE METHODS  ///////////////
    
    /**
     * Delegate creating of a Criterion to an appropriate method according to 
     * operator. 
     * 
     * Property name MUST be prefixed with an association alias!
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param operator comparison operator
     * @param argument argument
     * @return Criterion
     */
    protected Criterion createCriterion(String propertyPath, Comparison operator, Object argument) {
        LOG.trace("Creating criterion: {} {} {}", 
                new Object[]{propertyPath, operator, argument});
        
        switch (operator) {
            case EQUAL : {
                if (containWildcard(argument)) {
                    return createLike(propertyPath, argument);
                } else if (isNullArgument(argument)) {
                    return createIsNull(propertyPath);
                } else {
                    return createEqual(propertyPath, argument);
                }
            }
            case NOT_EQUAL : {
                if (containWildcard(argument)) {
                    return createNotLike(propertyPath, argument);
                } else if (isNullArgument(argument)) {
                    return createIsNotNull(propertyPath);
                } else {
                    return createNotEqual(propertyPath, argument);
                }
            }
            case GREATER_THAN : return createGreaterThan(propertyPath, argument);
            case GREATER_EQUAL : return createGreaterEqual(propertyPath, argument);
            case LESS_THAN : return createLessThan(propertyPath, argument);
            case LESS_EQUAL : return createLessEqual(propertyPath, argument);
        }
        throw new IllegalArgumentException("Unknown operator: " + operator);
    }
    
    /**
     * Apply an "equal" constraint to the named property.
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    protected Criterion createEqual(String propertyPath, Object argument) {
        return Restrictions.eq(propertyPath, argument);
    }
    
    /**
     * Apply a case-insensitive "like" constraint to the named property. Value
     * should contains wildcards "*" (% in SQL) and "_".
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    protected Criterion createLike(String propertyPath, Object argument) {
        String like = (String)argument;
        like = like.replace(LIKE_WILDCARD, '%');
        
        return Restrictions.ilike(propertyPath, like);
    }

    /**
     * Apply an "is null" constraint to the named property.
     *
     * @param propertyPath property name prefixed with an association alias
     * @return Criterion
     */
    protected Criterion createIsNull(String propertyPath) {
        return Restrictions.isNull(propertyPath);
    }
    
    /**
     * Apply a "not equal" constraint to the named property.
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    protected Criterion createNotEqual(String propertyPath, Object argument) {
        return Restrictions.ne(propertyPath, argument);
    }
    
    /**
     * Apply a negative case-insensitive "like" constraint to the named property. 
     * Value should contains wildcards "*" (% in SQL) and "_".
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument Value with wildcards.
     * @return Criterion
     */
    protected Criterion createNotLike(String propertyPath, Object argument) {        
        return Restrictions.not(createLike(propertyPath, argument));
    }

    /**
     * Apply an "is not null" constraint to the named property.
     *
     * @param propertyPath property name prefixed with an association alias
     * @return Criterion
     */
    protected Criterion createIsNotNull(String propertyPath) {
        return Restrictions.isNotNull(propertyPath);
    }
    
    /**
     * Apply a "greater than" constraint to the named property.
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    protected Criterion createGreaterThan(String propertyPath, Object argument) {
        return Restrictions.gt(propertyPath, argument);
    }
    
    /**
     * Apply a "greater than or equal" constraint to the named property.
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    protected Criterion createGreaterEqual(String propertyPath, Object argument) {
        return Restrictions.ge(propertyPath, argument);
    }
    
    /**
     * Apply a "less than" constraint to the named property.
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    protected Criterion createLessThan(String propertyPath, Object argument) {
        return Restrictions.lt(propertyPath, argument);
    }
    
    /**
     * Apply a "less than or equal" constraint to the named property.
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    protected Criterion createLessEqual(String propertyPath, Object argument) {
        return Restrictions.le(propertyPath, argument);
    }
    
    /**
     * Check if given argument contains wildcard.
     * 
     * @param argument
     * @return Return <tt>true</tt> if argument contains wildcard
     *         {@value #LIKE_WILDCARD}.
     */
    protected boolean containWildcard(Object argument) {
        if (!(argument instanceof String)) {
            return false;
        }
        
        String casted = (String) argument;
        return casted.contains(LIKE_WILDCARD.toString());

    }
    
    /**
     * Check if entity of specified class metadata contains given property.
     * 
     * @param property property name
     * @param classMetadata entity metadata
     * @return <tt>true</tt> if specified class metadata contains given property,
     *         otherwise <tt>false</tt>.
     */
    protected boolean isPropertyName(String property, ClassMetadata classMetadata) {
        if(classMetadata == null) {
            return false;
        }
        String[] names = classMetadata.getPropertyNames();
        for (String name : names) {
            if (name.equals(property)) return true;
        }
        return false;
    }
    
    /**
     * Find the java type class of given named property in entity's metadata.
     * 
     * @param property property name
     * @param classMetadata entity metadata
     * @return The java type class of given property.
     * @throws HibernateException If entity does not contain such property.
     */
    protected Class<?> findPropertyType(String property, ClassMetadata classMetadata) 
            throws HibernateException {
        if(classMetadata == null) {
            return null;
        }
        return classMetadata.getPropertyType(property).getReturnedClass();
    }

    /**
     * @param argument
     * @return <tt>true</tt> if argument is null, <tt>false</tt> otherwise
     */
    protected boolean isNullArgument(Object argument) {
        return NULL_ARGUMENT.equals(argument);
    }

}
