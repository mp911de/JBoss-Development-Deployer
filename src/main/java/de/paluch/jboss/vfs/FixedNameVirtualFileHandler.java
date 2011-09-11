package de.paluch.jboss.vfs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.virtual.spi.VirtualFileHandler;

/**
 * VFS Handler mit fixen File-Names. <br>
 * <br>
 * Projekt: jboss-dev-deployer <br>
 * Autor: mark <br>
 * Last Change: <br>
 * $Id:$ <br>
 * <br>
 * Copyright (c): Mark Paluch 2011 <br>
 */
public class FixedNameVirtualFileHandler extends AbstractDelegatingVirtualFileHandler
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4098590979032483305L;
	private String pathName;
	private String name;
	private VirtualFileHandler parent;

	/**
	 * 
	 * @param delegate
	 * @param parent
	 * @param pathName
	 * @param name
	 */
	public FixedNameVirtualFileHandler(VirtualFileHandler delegate, VirtualFileHandler parent, String pathName,
			String name)
	{

		super(delegate);
		this.parent = parent;
		this.pathName = pathName;
		this.name = name;
	}
	/**
	 * @see de.paluch.jboss.vfs.AbstractDelegatingVirtualFileHandler#getPathName()
	 */
	@Override
	public String getPathName()
	{

		if (pathName != null)
		{
			return pathName;
		}

		return super.getPathName();
	}

	/**
	 * @see de.paluch.jboss.vfs.AbstractDelegatingVirtualFileHandler#getName()
	 */
	@Override
	public String getName()
	{

		if (name != null)
		{
			return name;
		}
		return super.getName();
	}

	/**
	 * @see org.jboss.virtual.spi.VirtualFileHandler#getChild(java.lang.String)
	 */
	public VirtualFileHandler getChild(String path) throws IOException
	{

		if (path.equals(""))
		{
			return this;
		}

		return delegate.getChild(path);
	}

	/**
	 * @see de.paluch.jboss.vfs.AbstractDelegatingVirtualFileHandler#getParent()
	 */
	@Override
	public VirtualFileHandler getParent() throws IOException
	{

		if (parent != null)
		{
			return parent;
		}
		return super.getParent();
	}

	/**
	 * @see de.paluch.jboss.vfs.AbstractDelegatingVirtualFileHandler#toURI()
	 */
	@Override
	public URI toURI() throws URISyntaxException
	{

		if (parent != null && name != null)
		{
			URI parentUri = parent.toURI();
			return new URI(parentUri.toString() + name);
		}

		return super.toURI();
	}
	/**
	 * @see de.paluch.jboss.vfs.AbstractDelegatingVirtualFileHandler#toVfsUrl()
	 */
	@Override
	public URL toVfsUrl() throws MalformedURLException, URISyntaxException
	{

		if (parent != null && name != null)
		{

			URL parentUrl = parent.toVfsUrl();
			return new URL(parentUrl.toString() + name);
		}

		return super.toVfsUrl();
	}

	/**
	 * @see de.paluch.jboss.vfs.AbstractDelegatingVirtualFileHandler#toURL()
	 */
	@Override
	public URL toURL() throws MalformedURLException, URISyntaxException
	{

		if (parent != null && name != null)
		{

			URL parentUrl = parent.toURL();
			return new URL(parentUrl.toString() + name);
		}

		return super.toURL();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{

		if (!super.equals(obj) && obj instanceof AbstractDelegatingVirtualFileHandler)
		{
			AbstractDelegatingVirtualFileHandler other = (AbstractDelegatingVirtualFileHandler) obj;

			try
			{
				if (other.toVfsUrl().toString().equals(toVfsUrl().toString()))
				{
					return true;
				}
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
			catch (URISyntaxException e)
			{
				e.printStackTrace();
			}

			return delegate.equals(other.delegate);
		}

		return false;
	}

}
