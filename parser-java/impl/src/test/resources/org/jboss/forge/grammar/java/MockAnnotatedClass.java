package org.jboss.forge.grammar.java;

import org.jboss.forge.test.grammar.java.common.MockAnnotation;

import static org.jboss.forge.test.grammar.java.common.MockEnum.FOO;

@Deprecated
@SuppressWarnings("deprecation")
@SuppressWarnings(value = "unchecked")
@MockAnnotation(FOO)
public class MockAnnotatedClass
{
   private String field;

   public MockAnnotatedClass()
   {
   }
}
