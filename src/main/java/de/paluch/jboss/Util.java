package de.paluch.jboss;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.deployers.plugins.structure.ClassPathEntryImpl;
import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.logging.Logger;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.spi.VirtualFileHandler;

import de.paluch.jboss.model.DevelopmentStructure;
import de.paluch.jboss.vfs.RedirectingVirtualFileHandler;

/**
 * Common Utilities. <br>
 * <br>
 * Projekt: jboss-dev-deployer <br>
 * Autor: mark <br>
 * Last Change: <br>
 * $Id:
 * //depot/JBoss/jboss-dev-deployer/src/main/java/de/paluch/jboss/Util.java#2 $ <br>
 * <br>
 * Copyright (c): Mark Paluch 2011 <br>
 */
public class Util
{

	private static Logger LOGGER = Logger.getLogger("JBossDevelopmentDeployer");

	private Util()
	{

	}

	/**
	 * Add Jars from Directory
	 * 
	 * @param context
	 * @param target
	 * @param externalFiles
	 * @throws IOException
	 */
	public static void addJars(ContextInfo context, File target, List<String> externalFiles) throws IOException
	{

		if (target.isDirectory())
		{
			File files[] = target.listFiles();
			for (File subfile : files)
			{
				if (subfile.getName().endsWith(".jar"))
				{
					addEntry(context, subfile, externalFiles);
				}
			}
		}
	}

	/**
	 * Add Class Path Entry.
	 * 
	 * @param context
	 * @param classPathFile
	 * @param externalFiles
	 * @return ClassPathEntry
	 * @throws IOException
	 */
	public static ClassPathEntry addEntry(ContextInfo context, File classPathFile, List<String> externalFiles)
			throws IOException
	{

		LOGGER.info("adding " + classPathFile.getCanonicalPath() + " to classpath");
		String path = classPathFile.getCanonicalPath();
		externalFiles.add(path);
		ClassPathEntry cpe = StructureMetaDataFactory.createClassPathEntry(path);
		context.addClassPathEntry(cpe);
		return cpe;
	}

	/**
	 * Switch VFS-Handler via Reflection.
	 * 
	 * @param file
	 * @param devStructure
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public static void switchHandlerReflection(VirtualFile file, DevelopmentStructure devStructure)
			throws NoSuchFieldException, IllegalAccessException
	{

		VirtualFileHandler original = getHandler(file);
		switchHandlerReflection(file, original, devStructure);
	}

	/**
	 * Switch VFS-Handler via Reflection.
	 * 
	 * @param file
	 * @param delegate
	 * @param devStructure
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public static void switchHandlerReflection(VirtualFile file, VirtualFileHandler delegate,
			DevelopmentStructure devStructure) throws NoSuchFieldException, IllegalAccessException
	{

		Field field = file.getClass().getDeclaredField("handler");

		field.setAccessible(true);

		VirtualFileHandler delegated = new RedirectingVirtualFileHandler(delegate, devStructure.getExternalFileNames(),
				devStructure.getDevelopmentEntries());

		field.set(file, delegated);
	}

	/**
	 * Add Development-Entry to Context.
	 * 
	 * @param deployer
	 * @param structureContext
	 * @param context
	 * @param file
	 * @param structure
	 * @param path
	 * @throws IOException
	 * @throws Exception
	 */
	public static void inspectClassPathFromVFSFile(IDevelopmentStructureDeployer deployer,
			StructureContext structureContext, ContextInfo context, VirtualFile file, DevelopmentStructure structure,
			String path) throws IOException, Exception
	{

		File classPathFile = new File(path);
		if (classPathFile.isAbsolute() && classPathFile.exists())
		{
			addJars(context, classPathFile, structure.getExternalFileNames());
			addEntry(context, classPathFile, structure.getExternalFileNames());
			return;

		}
		else if (path.startsWith(".."))
		{

			File base = new File(VFSUtils.getCompatibleURI(file));
			File target = new File(base, path);
			if (target.exists())
			{

				target = target.getCanonicalFile();

				addJars(context, target, structure.getExternalFileNames());
				addEntry(context, target, structure.getExternalFileNames());
				return;
			}

		}

		VirtualFile relative = file.getChild(path);
		if (relative != null)
		{
			LOGGER.info("adding " + path + " to classpath");
			structure.getExternalFileNames().add(path);
			deployer.addClassPath(structureContext, relative, true, true, context);
		}

		LOGGER.error("File " + path + " not found");

	}

	/**
	 * remove Class-Path-Duplicates and log Class-Path.
	 * 
	 * @param structureContext
	 */
	public static void removeAndLogDuplicates(StructureContext structureContext)
	{

		List<ContextInfo> contexts = structureContext.getMetaData().getContexts();
		Set<String> unique = new TreeSet<String>();

		for (ContextInfo contextInfo : contexts)
		{
			List<ClassPathEntry> entries = contextInfo.getClassPath();
			if (entries == null)
			{
				continue;
			}

			List<ClassPathEntry> toRemove = new ArrayList<ClassPathEntry>();
			toRemove.add(new ClassPathEntryImpl(""));
			for (ClassPathEntry classPathEntry : entries)
			{

				String path = classPathEntry.getPath();
				int index = path.lastIndexOf('/');
				if (index == -1)
				{
					index = path.lastIndexOf('\\');
				}

				if (index == -1)
				{
					index = path.lastIndexOf(File.separatorChar);
				}

				if (index != -1)
				{
					File file = new File(path);
					if (file.exists() && file.isDirectory())
					{
						continue;
					}

					String filename = path.substring(index + 1);
					if (unique.contains(filename))
					{
						LOGGER.info("Class-Path contains duplicate, removing duplicate from "
							+ classPathEntry.getPath());

						toRemove.add(classPathEntry);

					}
					else
					{
						unique.add(filename);
					}
				}
			}

			entries.removeAll(toRemove);
		}

		for (ContextInfo contextInfo : contexts)
		{
			List<ClassPathEntry> entries = contextInfo.getClassPath();
			if (entries == null)
			{
				continue;
			}

			for (ClassPathEntry classPathEntry : entries)
			{
				LOGGER.info("Class-Path: " + classPathEntry.getPath());
			}
		}

	}

	/**
	 * Get VirtualFileHandler via Reflection.
	 * 
	 * @param file
	 * @return VirtualFileHandler
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public static VirtualFileHandler getHandler(VirtualFile file) throws NoSuchFieldException, IllegalAccessException
	{

		Field field = file.getClass().getDeclaredField("handler");

		field.setAccessible(true);
		return (VirtualFileHandler) field.get(file);
	}

}
