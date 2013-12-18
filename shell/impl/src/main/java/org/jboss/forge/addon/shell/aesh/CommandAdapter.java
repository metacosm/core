/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.shell.aesh;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.shell.ShellImpl;
import org.jboss.forge.addon.shell.ShellMessages;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.validation.UIValidationMessage;
import org.jboss.forge.addon.ui.validation.UIValidationMessage.Severity;

/**
 * Adapts the current {@link AbstractShellInteraction} to a {@link Command}
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
class CommandAdapter implements Command<CommandInvocation>
{
   private static final Logger log = Logger.getLogger(CommandAdapter.class.getName());

   private final ShellImpl shell;
   private final AbstractShellInteraction interaction;

   public CommandAdapter(ShellImpl shell, AbstractShellInteraction interaction)
   {
      this.shell = shell;
      this.interaction = interaction;
   }

   public List<UIValidationMessage> validate()
   {
      return interaction.getController().validate();
   }

   @SuppressWarnings("unchecked")
   @Override
   public CommandResult execute(CommandInvocation commandInvocation) throws IOException
   {
      boolean failure = false;
      if (interaction.getController().isValid())
      {
         Result result = null;
         try
         {
            result = interaction.getController().execute();
         }
         catch (Exception e)
         {
            log.log(Level.SEVERE, "Failed to execute [" + interaction.getName() + "] due to exception.", e);
            result = Results.fail(e.getMessage(), e);
         }

         if (result != null && result.getMessage() != null && !result.getMessage().isEmpty())
         {
            if (result instanceof Failed)
            {
               ShellMessages.error(shell.getConsole().getShell().err(), result.getMessage());
               failure = true;
            }
            else
            {
               ShellMessages.success(shell.getConsole().getShell().out(), result.getMessage());
               failure = false;
            }
         }

         Object selection = interaction.getContext().getSelection();
         if (selection != null)
         {
            if (selection instanceof Iterable<?>)
            {
               for (FileResource<?> item : (Iterable<FileResource<?>>) selection)
               {
                  if (item != null)
                  {
                     shell.setCurrentResource(item);
                     break;
                  }
               }
            }
            else
            {
               shell.setCurrentResource((FileResource<?>) selection);
            }
         }
      }
      else
      {
         List<UIValidationMessage> messages = interaction.getController().validate();
         for (UIValidationMessage message : messages)
         {
            if (message.getSeverity() == Severity.ERROR)
               ShellMessages.error(shell.getConsole().getShell().err(), message.getDescription());
         }
      }
      return failure ? CommandResult.FAILURE : CommandResult.SUCCESS;
   }
}