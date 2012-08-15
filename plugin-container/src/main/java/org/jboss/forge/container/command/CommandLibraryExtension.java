/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.container.command;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ProcessBean;

import org.jboss.forge.plugin.Alias;
import org.jboss.forge.plugin.Command;
import org.jboss.forge.plugin.DefaultCommand;
import org.jboss.forge.plugin.Option;
import org.jboss.forge.plugin.PipeIn;
import org.jboss.forge.plugin.PipeOut;
import org.jboss.forge.plugin.Plugin;
import org.jboss.forge.plugin.SetupCommand;
import org.jboss.forge.plugin.doc.Help;
import org.jboss.forge.plugin.doc.Topic;
import org.jboss.forge.plugin.meta.CommandMetadata;
import org.jboss.forge.plugin.meta.PluginMetadata;
import org.jboss.forge.util.Annotations;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class CommandLibraryExtension
{
   private final Map<String, List<PluginMetadata>> plugins = new HashMap<String, List<PluginMetadata>>();

   public Map<String, List<PluginMetadata>> getPlugins()
   {
      return plugins;
   }

   @SuppressWarnings("unchecked")
   public void scan(@Observes final ProcessBean<?> event)
   {
      Bean<?> bean = event.getBean();

      Class<?> clazz = bean.getBeanClass();

      if (Plugin.class.isAssignableFrom(clazz))
      {
         PluginMetadata pluginMeta = getMetadataFor((Class<? extends Plugin>) clazz);

         if (!plugins.containsKey(pluginMeta.getName()))
         {
            plugins.put(pluginMeta.getName(), new ArrayList<PluginMetadata>());
         }

         plugins.get(pluginMeta.getName()).add(pluginMeta);
      }
   }

   public PluginMetadata getMetadataFor(final Class<? extends Plugin> plugin)
   {
      String name = getPluginName(plugin);

      PluginMetadataImpl pluginMeta = new PluginMetadataImpl();
      pluginMeta.setName(name);
      pluginMeta.setType(plugin);

      if (Annotations.isAnnotationPresent(plugin, Help.class))
      {
         pluginMeta.setHelp(Annotations.getAnnotation(plugin, Help.class).value());
      }
      else
      {
         pluginMeta.setHelp("");
      }

      if (Annotations.isAnnotationPresent(plugin, Topic.class))
      {
         pluginMeta.setTopic(Annotations.getAnnotation(plugin, Topic.class).value());
      }

      processPluginCommands(pluginMeta, plugin);

      return pluginMeta;
   }

   @SuppressWarnings("rawtypes")
   private List<CommandMetadata> processPluginCommands(final PluginMetadataImpl pluginMeta, final Class<?> plugin)
   {
      List<CommandMetadata> results = new ArrayList<CommandMetadata>();

      for (Method method : plugin.getMethods())
      {
         if (Annotations.isAnnotationPresent(method, Command.class))
         {
            Command command = Annotations.getAnnotation(method, Command.class);
            CommandMetadataImpl commandMeta = new CommandMetadataImpl();
            commandMeta.setMethod(method);
            commandMeta.setHelp(command.help());
            commandMeta.setParent(pluginMeta);

            // Default commands are invoked via the name of the plug-in, not by
            // plug-in + command
            if ("".equals(command.value()))
            {
               commandMeta.setName(method.getName().trim().toLowerCase());
            }
            else
            {
               commandMeta.setName(command.value());
            }

            // This works because @DefaultCommand is annotated by @Command
            if (Annotations.isAnnotationPresent(method, DefaultCommand.class))
            {
               if (pluginMeta.hasDefaultCommand())
               {
                  throw new IllegalStateException("Plugins may only have one @"
                           + DefaultCommand.class.getSimpleName()
                           + ", but [" + pluginMeta.getType() + "] has more than one.");
               }

               commandMeta.setDefault(true);
               commandMeta.setName(pluginMeta.getName());

               // favor help text from this annotation over others
               DefaultCommand def = Annotations.getAnnotation(method, DefaultCommand.class);
               if ((def.help() != null) && !def.help().trim().isEmpty())
               {
                  commandMeta.setHelp(def.help());
               }
            }

            // This works because @SetupCommand is annotated by @Command
            if (Annotations.isAnnotationPresent(method, SetupCommand.class))
            {
               if (pluginMeta.hasSetupCommand())
               {
                  throw new IllegalStateException("Plugins may only have one @"
                           + SetupCommand.class.getSimpleName()
                           + ", but [" + pluginMeta.getType() + "] has more than one.");
               }

               commandMeta.setSetup(true);
               commandMeta.setName("setup");

               // favor help text from this annotation over others
               SetupCommand def = Annotations.getAnnotation(method, SetupCommand.class);
               if ((def.help() != null) && !def.help().trim().isEmpty())
               {
                  commandMeta.setHelp(def.help());
               }
            }

            // fall back to the pluginMetadata for help text
            if ((commandMeta.getHelp() == null) || commandMeta.getHelp().trim().isEmpty())
            {
               commandMeta.setHelp(pluginMeta.getHelp());
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();

            int i = 0;
            for (Class<?> clazz : parameterTypes)
            {
               OptionMetadataImpl optionMeta = new OptionMetadataImpl();

               optionMeta.setType(clazz);
               optionMeta.setIndex(i);

               if (PipeOut.class.isAssignableFrom(clazz))
               {
                  optionMeta.setPipeOut(true);
               }

               for (Annotation annotation : parameterAnnotations[i])
               {
                  if (annotation instanceof Option)
                  {
                     Option option = (Option) annotation;
                     optionMeta.setParent(commandMeta);
                     optionMeta.setName(option.name());
                     optionMeta.setShortName(option.shortName());
                     optionMeta.setFlagOnly(option.flagOnly());
                     optionMeta.setDescription(option.description());
                     optionMeta.setDefaultValue(option.defaultValue());
                     optionMeta.setHelp(option.help());
                     optionMeta.setRequired(option.required());

                  }
                  else if (annotation instanceof PipeIn)
                  {
                     optionMeta.setPipeIn(true);
                  }

               }
               commandMeta.addOption(optionMeta);

               i++;
            }

            results.add(commandMeta);
         }
      }

      pluginMeta.addCommands(results);

      return results;
   }

   private String getPluginName(final Class<?> plugin)
   {
      String name = null;

      if (Annotations.isAnnotationPresent(plugin, Alias.class))
      {
         Alias named = Annotations.getAnnotation(plugin, Alias.class);
         if (named != null)
         {
            name = named.value();
         }
      }

      if ((name == null) || "".equals(name.trim()))
      {
         name = plugin.getSimpleName();
      }
      return name.toLowerCase();
   }
}
