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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NaturalIdCollectionCriterionBuilder extends IdentifierCollectionCriterionBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(NaturalIdCollectionCriterionBuilder.class);

    @Override
    public boolean accept(String property, Class<?> entityClass, CriteriaBuilder builder) {

        if (super.accept(property, entityClass, builder)) {
            Class<?> assocType = findCollectionElementType(property, entityClass, builder);

            return builder.getClassMetadata(assocType).hasNaturalIdentifier();
        }
        return false;
    }

    @Override
    public Criterion createCriterion(String property, Comparison operator,
            String argument, Class<?> entityClass, String alias, CriteriaBuilder builder)
            throws ArgumentFormatException, UnknownSelectorException {

        Class<?> elemType = findCollectionElementType(property, entityClass, builder);

        ClassMetadata classMetadata = builder.getClassMetadata(elemType);
        int[] idProps = classMetadata.getNaturalIdentifierProperties();
        Class<?> idType = classMetadata.getPropertyTypes()[idProps[0]].getReturnedClass();
        String idName = classMetadata.getPropertyNames()[idProps[0]];

        if (idProps.length != 1) {
            LOG.warn("Entity {} has more than one Natural ID, only first will be used");
        }
        LOG.debug("Entity {} has Natural ID {} of type {}",
                new Object[]{elemType.getSimpleName(), idName, idType.getSimpleName()});

        Object castedArgument = builder.getArgumentParser().parse(argument, idType);

        String newAlias = builder.createAssociationAlias(alias + property);

        return createCriterion(newAlias +'.'+ idName, operator, castedArgument);
    }
}
