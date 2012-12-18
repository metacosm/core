/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.parser.java.impl;

import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.jboss.forge.parser.java.JavaAnnotation;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.parser.java.SourceType;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class JavaAnnotationImpl extends AbstractJavaSource<JavaAnnotation> implements JavaAnnotation
{

   public JavaAnnotationImpl(JavaSource<?> enclosingType, final Document document, final CompilationUnit unit,
            BodyDeclaration body)
   {
      super(enclosingType, document, unit, body);
   }

   @Override
   protected JavaAnnotation updateTypeNames(final String name)
   {
      return this;
   }

   @Override
   public SourceType getSourceType()
   {
      return SourceType.ANNOTATION;
   }

}
