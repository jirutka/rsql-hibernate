/*
 * The MIT License
 *
 * Copyright 2013-2014 Jakub Jirutka <jakub@jirutka.cz>.
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
package cz.jirutka.rsql.hibernate;

import cz.jirutka.rsql.parser.model.Comparison;
import org.hibernate.criterion.Criterion;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentifierCollectionCriterionBuilder extends AbstractCriterionBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(IdentifierCollectionCriterionBuilder.class);

    public boolean accept(String property, Class<?> entityClass, CriteriaBuilder builder) {

        ClassMetadata metadata = builder.getClassMetadata(entityClass);
        if (isPropertyName(property, metadata)) {
            Type type = metadata.getPropertyType(property);

            return type.isCollectionType() && type.isAssociationType();
        }
        return false;
    }

    public Criterion createCriterion(String property, Comparison operator,
            String argument, Class<?> entityClass, String alias, CriteriaBuilder builder)
            throws ArgumentFormatException, UnknownSelectorException {

        Class<?> elemType = findCollectionElementType(property, entityClass, builder);

        ClassMetadata assocClassMetadata = builder.getClassMetadata(elemType);
        Class<?> idType = assocClassMetadata.getIdentifierType().getReturnedClass();

        LOG.debug("Property is a collection of associations type {}, parsing argument to ID type {}",
                elemType, idType.getSimpleName());

        Object parsedArgument = builder.getArgumentParser().parse(argument, idType);

        String newAlias = builder.createAssociationAlias(alias + property);
        String propertyPath = newAlias + ".id";

        return createCriterion(propertyPath, operator, parsedArgument);
    }
}
