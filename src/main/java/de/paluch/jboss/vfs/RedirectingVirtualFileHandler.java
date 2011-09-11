package de.paluch.jboss.vfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.virtual.plugins.context.file.FileSystemContextFactory;
import org.jboss.virtual.spi.VFSContext;
import org.jboss.virtual.spi.VirtualFileHandler;

import de.paluch.jboss.model.DevelopmentEntry;
import de.paluch.jboss.model.WebAppDevelopmentEntry;

/**
 * Virtual File Handler for Development purposes. Due to Child-Only-Constraint
 * of JBoss VFS a special implementation needed. This VFS Handler allows a Set
 * of external Files, which can be accessed thru getChild(). <br>
 * <br>
 * Projekt: jboss-dev-deployer <br>
 * Autor: mark <br>
 * Last Change: <br>
 * $Id: //depot/JBoss/jboss-dev-deployer/src/main/java/org/jboss/virtual/
 * DevVirtualFileHandler.java#4 $ <br>
 * <br>
 */
public class RedirectingVirtualFileHandler extends AbstractDelegatingVirtualFileHandler
{

	/**
	 *
	 */
	private static final long serialVersionUID = 7544479459827500609L;
	private Collection<String> allowedFSChildren;
	private Collection<DevelopmentEntry> devEntries = null;

	/**
	 * @param handler
	 * @param allowedFSChildren
	 */
	public RedirectingVirtualFileHandler(VirtualFileHandler handler, Collection<String> allowedFSChildren)
	{

		super(handler);
		this.allowedFSChildren = allowedFSChildren;
	}

	/**
	 * @param handler
	 * @param allowedFSChildren
	 * @param devEntries
	 */
	public RedirectingVirtualFileHandler(VirtualFileHandler handler, Collection<String> allowedFSChildren,
			Collection<DevelopmentEntry> devEntries)
	{

		super(handler);
		this.allowedFSChildren = allowedFSChildren;
		this.devEntries = devEntries;
	}

	/**
	 * @see de.paluch.jboss.vfs.AbstractDelegatingVirtualFileHandler#getChildren(boolean)
	 */
	@Override
	public List<VirtualFileHandler> getChildren(boolean ignoreErrors) throws IOException
	{

		List<VirtualFileHandler> delegateChildren = super.getChildren(ignoreErrors);

		List<VirtualFileHandler> result = new ArrayList<VirtualFileHandler>();

		for (VirtualFileHandler child : delegateChildren)
		{
			VirtualFileHandler substitue = getChild(child.getName());
			if (substitue instanceof FixedNameVirtualFileHandler)
			{
				result.add(substitue);
			}
			else
			{
				result.add(child);
			}
		}

		return result;

	}

	/**
	 * @param path
	 * @throws IOException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#getChild(java.lang.String)
	 */
	public VirtualFileHandler getChild(String path) throws IOException
	{

		if (path.equals(""))
		{
			return this;
		}

		DevelopmentEntry entry = getDevEntry(path);

		VirtualFileHandler childHandler = delegate.getChild(path);

		if (entry instanceof WebAppDevelopmentEntry)
		{
			return childHandler;
		}

		if (allowedFSChildren != null && (childHandler == null || entry != null))
		{
			String mappedPath = path;

			File file = null;

			if (entry != null)
			{
				file = new File(entry.getName());
			}

			if (allowedFSChildren.contains(mappedPath))
			{
				file = new File(mappedPath);
			}

			if (file != null && file.exists())
			{
				VFSContext context = new FileSystemContextFactory().getVFS(file.toURI());
				FixedNameVirtualFileHandler handler = null;

				if (entry != null)
				{
					handler = new FixedNameVirtualFileHandler(context.getRoot(), this, getPathName() + "/"
						+ entry.getId(), entry.getId());
				}
				else
				{
					handler = new FixedNameVirtualFileHandler(context.getRoot(), this, null, null);
				}

				return handler;

			}
		}

		return childHandler;
	}

	/**
	 * @param path
	 * @return DevelopmentEntry oder null
	 */
	protected DevelopmentEntry getDevEntry(String path)
	{

		if (devEntries != null)
		{
			for (DevelopmentEntry devEntry : devEntries)
			{
				if (devEntry.getId().equalsIgnoreCase(path) || devEntry.getName().equalsIgnoreCase(path))
				{
					return devEntry;
				}
			}
		}

		return null;
	}

}
