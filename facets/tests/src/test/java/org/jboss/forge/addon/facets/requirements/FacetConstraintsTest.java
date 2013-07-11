package org.jboss.forge.addon.facets.requirements;

/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.facets.MockFacet;
import org.jboss.forge.addon.facets.MockFaceted;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class FacetConstraintsTest
{
   @Deployment
   @Dependencies({
            @AddonDependency(name = "org.jboss.forge.addon:facets", version = "2.0.0-SNAPSHOT"),
            @AddonDependency(name = "org.jboss.forge.furnace:container-cdi", version = "2.0.0-SNAPSHOT")
   })
   public static ForgeArchive getDeployment()
   {
      ForgeArchive archive = ShrinkWrap
               .create(ForgeArchive.class)
               .addClasses(FacetA.class, FacetB.class, FacetC.class, MockFaceted.class, MockFacet.class)
               .addBeansXML()
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.addon:facets", "2.0.0-SNAPSHOT"),
                        AddonDependencyEntry.create("org.jboss.forge.furnace:container-cdi", "2.0.0-SNAPSHOT")
               );
      return archive;
   }

   @Inject
   private FacetFactory facetFactory;

   @Test
   public void testFacetFactoryInstallationInstallsDependencies() throws Exception
   {
      MockFaceted faceted = new MockFaceted();
      facetFactory.install(faceted, FacetA.class);

      Assert.assertTrue(faceted.hasFacet(FacetA.class));
      Assert.assertTrue(faceted.hasFacet(FacetB.class));
      Assert.assertTrue(faceted.hasFacet(FacetC.class));
   }

   @Test
   public void testProjectFacetInstallationInstallsDependencies() throws Exception
   {
      MockFaceted faceted = new MockFaceted();
      facetFactory.install(faceted, FacetB.class);

      Assert.assertTrue(faceted.hasFacet(FacetB.class));
      Assert.assertTrue(faceted.hasFacet(FacetC.class));
   }

}
