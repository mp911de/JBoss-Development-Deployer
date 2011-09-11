package de.paluch.jboss;

import java.io.IOException;

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.virtual.VirtualFile;

/**
 * Structure Deployer for Class-Path Handling. <br>
 * <br>
 * Projekt: jboss-dev-deployer <br>
 * Autor: mark <br>
 * Last Change: <br>
 * $Id:$ <br>
 * <br>
 * Copyright (c): Mark Paluch 2011 <br>
 */
public interface IDevelopmentStructureDeployer
{

	/**
	 * add VFS File to Class-Path
	 * 
	 * @param structureContext
	 * @param entry
	 * @param includeEntry
	 * @param includeRootManifestCP
	 * @param context
	 * @throws IOException
	 */
	public void addClassPath(StructureContext structureContext, VirtualFile entry, boolean includeEntry,
			boolean includeRootManifestCP, ContextInfo context) throws IOException;
}
