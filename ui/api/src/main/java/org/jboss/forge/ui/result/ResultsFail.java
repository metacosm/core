/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.ui.result;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ResultsFail extends Results
{
   ResultsFail(String message)
   {
      super(message);
   }

   public ResultsFail(String message, Throwable e)
   {
      super(message, e);
   }
}