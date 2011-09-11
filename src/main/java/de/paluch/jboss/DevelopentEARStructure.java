package de.paluch.jboss;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.deployment.EARStructure;
import org.jboss.deployment.J2eeModuleMetaData;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.metadata.ear.jboss.ServiceModuleMetaData;
import org.jboss.metadata.ear.spec.AbstractModule;
import org.jboss.metadata.ear.spec.ConnectorModuleMetaData;
import org.jboss.metadata.ear.spec.EarMetaData;
import org.jboss.metadata.ear.spec.EjbModuleMetaData;
import org.jboss.metadata.ear.spec.JavaModuleMetaData;
import org.jboss.metadata.ear.spec.ModuleMetaData;
import org.jboss.metadata.ear.spec.ModulesMetaData;
import org.jboss.metadata.ear.spec.WebModuleMetaData;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.plugins.context.file.FileSystemContextFactory;
import org.jboss.virtual.spi.VFSContext;
import org.jboss.virtual.spi.VirtualFileHandler;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

import de.paluch.jboss.model.DevelopmentEntry;
import de.paluch.jboss.model.DevelopmentStructure;
import de.paluch.jboss.vfs.FixedNameVirtualFileHandler;

/**
 * EAR Development Structure. <br>
 * <br>
 * Projekt: jboss-dev-deployer <br>
 * Autor: mark <br>
 * Last Change: <br>
 * $Id: //depot/JBoss/jboss-dev-deployer/src/main/java/de/paluch/jboss/
 * DevelopentEARStructure.java#2 $ <br>
 * <br>
 * Copyright (c): Mark Paluch 2011 <br>
 */
public class DevelopentEARStructure extends EARStructure implements IDevelopmentStructureDeployer
{

	@Override
	public boolean determineStructure(StructureContext structureContext) throws DeploymentException
	{

		ContextInfo context;
		boolean valid;

		VirtualFile file = structureContext.getFile();
		try
		{
			if (file.isLeaf() == true || file.getName().endsWith(".ear") == false)
			{
				return false;
			}

			context = createContext(structureContext, "META-INF");

			VirtualFile applicationXml = getMetaDataFile(file, "META-INF/application.xml");
			VirtualFile jbossAppXml = getMetaDataFile(file, "META-INF/jboss-app.xml");
			VirtualFile jbossDevelopmentDeployer = getMetaDataFile(file, "META-INF/jboss-development.xml");
			Map<String, String> fileNameMapping = new HashMap<String, String>();

			boolean scan = true;

			Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
			unmarshaller.setValidation(isUseValidation());
			EarMetaData specMetaData = null;
			JBossAppMetaData appMetaData = null;
			DevelopmentStructure devStructure = null;
			if (applicationXml != null)
			{
				specMetaData = getSpecMetaData(applicationXml, unmarshaller);
				scan = false;
			}
			appMetaData = getAppMetaData(jbossAppXml, unmarshaller, appMetaData);

			devStructure = getDevEntries(jbossDevelopmentDeployer);
			if (devStructure != null)
			{
				Util.switchHandlerReflection(file, devStructure);
			}

			// Need a metadata instance and there will not be one if there are
			// no descriptors
			if (appMetaData == null)
			{
				appMetaData = new JBossAppMetaData();
			}
			// Create the merged view
			appMetaData.merge(appMetaData, specMetaData);

			setupClassPath(structureContext, context, file, fileNameMapping, appMetaData, devStructure);

			// Add the ear manifest locations?
			addClassPath(structureContext, file, true, true, context);

			// TODO: need to scan for annotationss
			if (scan)
			{
				scanEar(file, appMetaData, devStructure, fileNameMapping);
			}

			// Create subdeployments for the ear modules
			ModulesMetaData modules = appMetaData.getModules();

			if (devStructure != null)
			{
				// handleUnusedEntries(devStructure, file, structureContext);
			}

			createSubDeployments(structureContext, file, devStructure, modules);
			valid = true;

			handleUnusedEntries(devStructure, file, structureContext, context);
			Util.removeAndLogDuplicates(structureContext);

		}
		catch (Exception e)
		{
			throw new RuntimeException("Error determining structure: " + file.getName(), e);
		}

		return valid;
	}

	/**
	 * @param structureContext
	 * @param context
	 * @param file
	 * @param fileNameMapping
	 * @param appMetaData
	 * @param devStructure
	 * @throws Exception
	 * @throws DeploymentException
	 */
	private void setupClassPath(StructureContext structureContext, ContextInfo context, VirtualFile file,
			Map<String, String> fileNameMapping, JBossAppMetaData appMetaData, DevelopmentStructure devStructure)
			throws Exception, DeploymentException
	{

		VirtualFile lib;

		boolean trace = log.isTraceEnabled();

		String libDir = appMetaData.getLibraryDirectory();
		if (libDir == null || libDir.length() > 0)
		{
			if (libDir == null)
			{
				libDir = "lib";
			}

			// Add the ear lib contents to the classpath
			if (trace)
			{
				log.trace("Checking for ear lib directory: " + libDir);
			}
			try
			{
				lib = file.getChild(libDir);
				if (lib != null)
				{
					if (trace)
					{
						log.trace("Found ear lib directory: " + lib);
					}
					List<VirtualFile> archives = lib.getChildren(DEFAULT_EAR_LIB_FILTER);
					for (VirtualFile archive : archives)
					{

						if (devStructure != null && devStructure.getFileNameMap().containsKey(archive.getName()))
						{
							DevelopmentEntry entry = devStructure.getFileNameMap().get(archive.getName());
							entry.setUsed(true);
							Util.inspectClassPathFromVFSFile(this, structureContext, context, file, devStructure, entry.getName());
							fileNameMapping.put(archive.getName(), entry.getName());

						}
						else
						{
							addClassPath(structureContext, archive, true, true, context);
							try
							{
								// add any jars with persistence.xml as a
								// deployment
								if (archive.getChild("META-INF/persistence.xml") != null)
								{
									log.trace(archive.getName() + " in ear lib directory has persistence units");
									if (structureContext.determineChildStructure(archive) == false)
									{
										throw new RuntimeException(
												archive.getName()
													+ " in lib directory has persistence.xml but is not a recognized deployment, .ear: "
													+ file.getName());
									}
								}
								else if (trace)
								{
									log.trace(archive.getPathName() + " does not contain META-INF/persistence.xml");
								}

							}
							catch (IOException e)
							{
								// TODO - should we throw this fwd?
								log.warn("Exception searching for META-INF/persistence.xml in " + archive.getPathName()
									+ ", " + e);
							}
						}

					}
				}
				else if (trace)
				{
					log.trace("No lib directory in ear archive.");
				}
			}
			catch (IOException e)
			{
				// TODO - should we throw this fwd?
				log.warn("Exception while searching for lib dir: " + e);
			}
		}
		else if (trace)
		{
			log.trace("Ignoring library directory, got empty library-directory element.");
		}
	}

	/**
	 * @param structureContext
	 * @param file
	 * @param devStructure
	 * @param modules
	 * @throws URISyntaxException
	 * @throws DeploymentException
	 */
	private void createSubDeployments(StructureContext structureContext, VirtualFile file,
			DevelopmentStructure devStructure, ModulesMetaData modules) throws URISyntaxException, DeploymentException
	{

		if (modules != null)
		{
			for (ModuleMetaData mod : modules)
			{
				String fileName = mod.getFileName();
				if (fileName != null && (fileName = fileName.trim()).length() > 0)
				{
					if (log.isTraceEnabled())
					{
						log.trace("Checking application.xml module: " + fileName);
					}

					try
					{
						VirtualFile module = file.getChild(fileName);

						if (devStructure != null && devStructure.getFileNameMap().containsKey(fileName))
						{
							DevelopmentEntry entry = devStructure.getFileNameMap().get(fileName);
							entry.setUsed(true);
							VFSContext vfs = new FileSystemContextFactory().getVFS(new File(entry.getName()).toURI());
							VirtualFileHandler handler = vfs.getRoot();
							FixedNameVirtualFileHandler fixed = new FixedNameVirtualFileHandler(handler,
									Util.getHandler(file), file.getPathName() + "/" + fileName, fileName);

							module = fixed.getVirtualFile();

						}

						if (module == null)
						{
							File realFile = new File(fileName);
							if (!realFile.exists())
							{
								throw new RuntimeException(fileName
									+ " module listed in application.xml does not exist " + file.toURI());
							}

						}
						// Ask the deployers to analyze this
						if (structureContext.determineChildStructure(module) == false)
						{
							throw new RuntimeException(fileName
								+ " module listed in application.xml is not a recognized deployment, .ear: "
								+ file.getName());
						}
					}
					catch (Exception e)
					{
						throw new RuntimeException("Exception looking for " + fileName
							+ " module listed in application.xml, .ear " + file.getName(), e);
					}
				}
			}
		}
	}

	/**
	 * Add unused Libs to Class-Path.
	 * 
	 * @param devStructure
	 * @param file
	 * @param structureContext
	 * @param context
	 * @param externalFiles
	 * @throws IOException
	 * @throws Exception
	 */
	private void handleUnusedEntries(DevelopmentStructure devStructure, VirtualFile file,
			StructureContext structureContext, ContextInfo context) throws IOException, Exception
	{

		for (DevelopmentEntry developmentEntry : devStructure.getDevelopmentEntries())
		{
			if (!developmentEntry.isUsed())
			{
				developmentEntry.setUsed(true);
				log.info("adding additional Entry " + developmentEntry.getName());
				Util.inspectClassPathFromVFSFile(this, structureContext, context, file, devStructure, developmentEntry.getName());
			}
		}

	}

	/**
	 * @param applicationXml
	 * @param unmarshaller
	 * @return
	 * @throws IOException
	 * @throws JBossXBException
	 */
	private EarMetaData getSpecMetaData(VirtualFile applicationXml, Unmarshaller unmarshaller) throws IOException,
			JBossXBException
	{

		EarMetaData specMetaData;
		InputStream in = applicationXml.openStream();
		try
		{
			specMetaData = (EarMetaData) unmarshaller.unmarshal(in, getResolver());
		}
		finally
		{
			in.close();
		}
		return specMetaData;
	}

	/**
	 * @param jbossAppXml
	 * @param unmarshaller
	 * @param appMetaData
	 * @return
	 * @throws IOException
	 * @throws JBossXBException
	 */
	private JBossAppMetaData getAppMetaData(VirtualFile jbossAppXml, Unmarshaller unmarshaller,
			JBossAppMetaData appMetaData) throws IOException, JBossXBException
	{

		if (jbossAppXml != null)
		{
			InputStream in = jbossAppXml.openStream();
			try
			{
				appMetaData = (JBossAppMetaData) unmarshaller.unmarshal(in, getResolver());
			}
			finally
			{
				in.close();
			}
		}
		return appMetaData;
	}

	/**
	 * @param jbossDevelopmentDeployer
	 * @param devEntries
	 * @return
	 * @throws IOException
	 * @throws DeploymentException
	 */
	private DevelopmentStructure getDevEntries(VirtualFile jbossDevelopmentDeployer) throws IOException,
			DeploymentException
	{

		if (jbossDevelopmentDeployer != null)
		{
			InputStream in = jbossDevelopmentDeployer.openStream();
			try
			{
				return new JBossDevelopmentParser().parseXml(in);
			}
			finally
			{
				in.close();
			}
		}

		return null;
	}

	private VirtualFile getFile(VirtualFile file, String path, List<String> externalFiles) throws IOException
	{

		File classPathFile = new File(path);
		if (classPathFile.isAbsolute())
		{
			if (!classPathFile.exists())
			{
				log.error("File " + classPathFile + " not found");
				return null;
			}
			externalFiles.add(classPathFile.getCanonicalPath());
			return VFS.getRoot(classPathFile.toURI());
		}

		if (path.startsWith(".."))
		{

			File base;
			try
			{
				base = new File(VFSUtils.getCompatibleURI(file));
				File target = new File(base, path);
				if (!target.exists())
				{
					log.error("File " + classPathFile + " not found");
					return null;
				}

				target = target.getCanonicalFile();
				externalFiles.add(target.getCanonicalPath());
				return VFS.getRoot(target.toURI());
			}
			catch (Exception e)
			{
				throw new IOException(e);
			}

		}

		VirtualFile relative = file.getChild(path);
		if (relative == null)
		{
			log.error("File " + path + " not found");
			return null;
		}

		return relative;

	}

	/**
	 * For an ear without an application.xml, determine modules via: a. All ear
	 * modules with an extension of .war are considered web modules. The context
	 * root of the web module is the name of the file relative to the root of
	 * the application package, with the .war extension removed. b. All ear
	 * modules with extension of .rar are considered resource adapters. c. A
	 * directory named lib is considered to be the library directory, as
	 * described in Section�EE.8.2.1, �Bundled Libraries.� d. For all ear
	 * modules with a filename extension of .jar, but not in the lib directory,
	 * do the following: i. If the JAR file contains a META-INF/MANIFEST.MF file
	 * with a Main-Class attribute, or contains a
	 * META-INF/application-client.xml file, consider the jar file to be an
	 * application client module. ii. If the JAR file contains a
	 * META-INF/ejb-jar.xml file, or contains any class with an EJB component
	 * annotation (Stateless, etc.), consider the JAR file to be an EJB module.
	 * iii. All other JAR files are ignored unless referenced by a JAR file
	 * discovered above using one of the JAR file reference mechanisms such as
	 * the Class-Path header in a manifest file. TODO: rewrite using vfs
	 * 
	 * @param externalFiles
	 * @param fileNameMapping
	 */
	private void scanEar(VirtualFile root, JBossAppMetaData appMetaData, DevelopmentStructure devStructure,
			Map<String, String> fileNameMapping) throws IOException
	{

		List<VirtualFile> archives = root.getChildren();
		if (archives != null)
		{
			String earPath = root.getPathName();
			ModulesMetaData modules = appMetaData.getModules();
			if (modules == null)
			{
				modules = new ModulesMetaData();
				appMetaData.setModules(modules);
			}
			for (VirtualFile vfArchive : archives)
			{
				VirtualFile vf = vfArchive;
				String filename = earRelativePath(earPath, vf.getPathName());

				if (devStructure != null && devStructure.getFileNameMap().containsKey(filename))
				{
					DevelopmentEntry entry = devStructure.getFileNameMap().get(filename);
					entry.setUsed(true);

					fileNameMapping.put(filename, entry.getName());

					filename = entry.getName();
					vf = getFile(root, entry.getName(), devStructure.getExternalFileNames());

				}

				// Check if the module already exists, i.e. it is declared in
				// jboss-app.xml
				ModuleMetaData moduleMetaData = appMetaData.getModule(filename);
				int type = typeFromSuffix(filename, vf);
				if (type >= 0 && moduleMetaData == null)
				{
					moduleMetaData = new ModuleMetaData();
					AbstractModule module = null;
					switch (type)
					{
						case J2eeModuleMetaData.EJB :
							module = new EjbModuleMetaData();
							break;
						case J2eeModuleMetaData.CLIENT :
							module = new JavaModuleMetaData();
							break;
						case J2eeModuleMetaData.CONNECTOR :
							module = new ConnectorModuleMetaData();
							break;
						case J2eeModuleMetaData.SERVICE :
						case J2eeModuleMetaData.HAR :
							module = new ServiceModuleMetaData();
							break;
						case J2eeModuleMetaData.WEB :
							module = new WebModuleMetaData();
							break;
					}
					module.setFileName(filename);
					moduleMetaData.setValue(module);
					modules.add(moduleMetaData);
				}
			}
		}
	}

	private int typeFromSuffix(String path, VirtualFile archive) throws IOException
	{

		int type = -1;
		if (path.endsWith(".war"))
		{
			type = J2eeModuleMetaData.WEB;
		}
		else if (path.endsWith(".rar"))
		{
			type = J2eeModuleMetaData.CONNECTOR;
		}
		else if (path.endsWith(".har"))
		{
			type = J2eeModuleMetaData.HAR;
		}
		else if (path.endsWith(".sar"))
		{
			type = J2eeModuleMetaData.SERVICE;
		}
		else if (path.endsWith(".jar"))
		{
			// Look for a META-INF/application-client.xml
			VirtualFile mfFile = getMetaDataFile(archive, "META-INF/MANIFEST.MF");
			VirtualFile clientXml = getMetaDataFile(archive, "META-INF/application-client.xml");
			VirtualFile ejbXml = getMetaDataFile(archive, "META-INF/ejb-jar.xml");
			VirtualFile jbossXml = getMetaDataFile(archive, "META-INF/jboss.xml");

			if (clientXml != null)
			{
				type = J2eeModuleMetaData.CLIENT;
			}
			else if (mfFile != null)
			{
				Manifest mf = VFSUtils.readManifest(mfFile);
				Attributes attrs = mf.getMainAttributes();
				if (attrs.containsKey(Attributes.Name.MAIN_CLASS))
				{
					type = J2eeModuleMetaData.CLIENT;
				}
				else
				{
					// TODO: scan for annotations. Assume EJB for now
					type = J2eeModuleMetaData.EJB;
				}
			}
			else if (ejbXml != null || jbossXml != null)
			{
				type = J2eeModuleMetaData.EJB;
			}
			else
			{
				// TODO: scan for annotations. Assume EJB for now
				type = J2eeModuleMetaData.EJB;
			}
		}

		return type;
	}

	private String earRelativePath(String earPath, String pathName)
	{

		StringBuilder tmp = new StringBuilder(pathName);
		tmp.delete(0, earPath.length());
		return tmp.toString();
	}

	private VirtualFile getMetaDataFile(VirtualFile file, String path)
	{

		VirtualFile metaFile = null;
		try
		{
			metaFile = file.getChild(path);
		}
		catch (IOException ignored)
		{
		}
		return metaFile;
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
