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
import cz.jirutka.rsql.hibernate.util.PropertyPathUtil;
import cz.jirutka.rsql.parser.model.Comparison;
import org.hibernate.criterion.Criterion;
import org.hibernate.metadata.ClassMetadata;

/**
 * Default implementation of Criterion Builder that simply creates 
 * <tt>Criterion</tt> for a basic property (not association). This should be the 
 * last builder in stack because its <tt>accept()</tt> method always returns 
 * <tt>true</tt>. Before creating a Criterion, property name is checked if it's 
 * valid and {@link cz.jirutka.rsql.hibernate.exception.UnknownSelectorException} is thrown if not.
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class DefaultCriterionBuilder extends AbstractCriterionBuilder {
    
    
    @Override
    public boolean accept(String property, Class<?> entityClass, CriteriaBuilder builder) {
        return true;
    }
    
    @Override
    public Criterion createCriterion(String property, Comparison operator, 
            String argument, Class<?> entityClass, String alias, CriteriaBuilder builder) 
            throws ArgumentFormatException, UnknownSelectorException {
        ClassMetadata metadata = builder.getClassMetadata(entityClass);
        Class<?> type;
        if(metadata != null) {
            if (!isPropertyName(property, metadata)) {
                throw new UnknownSelectorException(property);
            }

            type = findPropertyType(property, builder.getClassMetadata(entityClass));
        } else {
            type = PropertyPathUtil.getPropertyClass(entityClass, property);
        }
        Object castedArgument = builder.getArgumentParser().parse(argument, type);
        
        return createCriterion(alias + property, operator, castedArgument);
    }
    
}
