/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.plugin;

import org.jboss.forge.util.Annotations;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class ConstraintInspector
{
   /**
    * Return the name of the given bean type.
    */
   public static String getName(final Class<?> type)
   {
      String result = type.getSimpleName();

      if (Annotations.isAnnotationPresent(type, Alias.class))
      {
         Alias annotation = Annotations.getAnnotation(type, Alias.class);
         if ((annotation.value() != null) && !annotation.value().trim().isEmpty())
         {
            result = annotation.value();
         }
      }

      return result;
   }
}
