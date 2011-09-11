package de.paluch.jboss;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.plugins.context.file.FileSystemContextFactory;
import org.jboss.virtual.spi.VFSContext;
import org.jboss.virtual.spi.VirtualFileHandler;
import org.jboss.web.deployers.WARStructure;

import de.paluch.jboss.model.DevelopmentEntry;
import de.paluch.jboss.model.DevelopmentStructure;
import de.paluch.jboss.model.WebAppDevelopmentEntry;

/**
 * Development WAR Structure Resolver. Determines a Structure and adds custom
 * Class-Path-Elements.<br>
 * <br>
 * Projekt: jboss-dev-deployer <br>
 * Autor: mark <br>
 * Last Change: <br>
 * $Id: //depot/JBoss/jboss-dev-deployer/src/main/java/de/paluch/jboss/
 * DevelopmentWARStructure.java#6 $ <br>
 * <br>
 */
public class DevelopmentWARStructure extends WARStructure implements IDevelopmentStructureDeployer
{

	/**
	 * @see org.jboss.web.deployers.WARStructure#determineStructure(org.jboss.deployers.vfs.spi.structure.StructureContext)
	 */
	@Override
	public boolean determineStructure(StructureContext structureContext) throws DeploymentException
	{

		VirtualFile file = structureContext.getFile();
		try
		{

			VirtualFile development = file.getChild("WEB-INF/jboss-development.xml");

			DevelopmentStructure structure = null;
			if (development != null)
			{
				structure = new JBossDevelopmentParser().parseXml(development.openStream());
				switchVFSHandler(file, structure);
			}

			boolean result = super.determineStructure(structureContext);
			if (!result)
			{
				return result;
			}

			if (development != null && structure != null)
			{
				ContextInfo context = structureContext.getMetaData().getContexts().get(0);

				for (DevelopmentEntry developmentEntry : structure.getDevelopmentEntries())
				{
					Util.inspectClassPathFromVFSFile(this, structureContext, context, file, structure,
							developmentEntry.getName());
				}

				Util.removeAndLogDuplicates(structureContext);

			}

		}
		catch (Exception e)
		{
			throw DeploymentException.rethrowAsDeploymentException("Error determining structure: " + file.getName(), e);
		}

		return true;
	}

	/**
	 * Switch VFS Handler.
	 * 
	 * @param file
	 * @param structure
	 * @throws Exception
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	private void switchVFSHandler(VirtualFile file, DevelopmentStructure structure) throws Exception, IOException,
			MalformedURLException, URISyntaxException, NoSuchFieldException, IllegalAccessException
	{

		List<WebAppDevelopmentEntry> webapp = structure.getWebAppEntries();

		if (!webapp.isEmpty())
		{
			// Redirect to Path
			DevelopmentEntry rootRedirect = webapp.get(0);
			VirtualFileHandler redirectionDelegate = null;

			File classPathFile = new File(rootRedirect.getName());
			if (!classPathFile.isAbsolute())
			{
				File base = new File(VFSUtils.getCompatibleURI(file));
				File target = new File(base, rootRedirect.getName());
				classPathFile = target.getCanonicalFile();
			}

			VFSContext context = new FileSystemContextFactory().getVFS(classPathFile.toURI());
			redirectionDelegate = context.getRoot();
			Util.switchHandlerReflection(file, redirectionDelegate, structure);

		}
		else
		{
			// Use Entries
			Util.switchHandlerReflection(file, structure);
		}
	}

	/**
	 * @see org.jboss.deployers.vfs.spi.structure.helpers.AbstractStructureDeployer#addClassPath(org.jboss.deployers.vfs.spi.structure.StructureContext,
	 *      org.jboss.virtual.VirtualFile, boolean, boolean,
	 *      org.jboss.deployers.spi.structure.ContextInfo)
	 */
	@Override
	public void addClassPath(StructureContext structureContext, VirtualFile entry, boolean includeEntry,
			boolean includeRootManifestCP, ContextInfo context) throws IOException
	{

		super.addClassPath(structureContext, entry, includeEntry, includeRootManifestCP, context);
	}

}
