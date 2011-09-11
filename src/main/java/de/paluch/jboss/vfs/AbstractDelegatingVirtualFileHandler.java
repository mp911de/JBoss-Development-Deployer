package de.paluch.jboss.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.spi.VFSContext;
import org.jboss.virtual.spi.VirtualFileHandler;

/**
 * Abstract VFS Handler with Delegate.<br>
 * <br>
 * Projekt: jboss-dev-deployer <br>
 * Autor: mark <br>
 * Last Change: <br>
 * $Id:$ <br>
 * <br>
 * Copyright (c): Mark Paluch 2011 <br>
 */
public abstract class AbstractDelegatingVirtualFileHandler implements VirtualFileHandler
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3849310354265296480L;
	protected VirtualFileHandler delegate;

	/**
	 * 
	 * @param delegate
	 */
	public AbstractDelegatingVirtualFileHandler(VirtualFileHandler delegate)
	{

		super();
		this.delegate = delegate;
	}

	/**
	 * @see org.jboss.virtual.spi.VirtualFileHandler#getName()
	 */
	public String getName()
	{

		return delegate.getName();
	}

	/**
	 * @see org.jboss.virtual.spi.VirtualFileHandler#getPathName()
	 */
	public String getPathName()
	{

		return delegate.getPathName();
	}

	/**
	 * @see org.jboss.virtual.spi.VirtualFileHandler#getLocalPathName()
	 */
	public String getLocalPathName()
	{

		return delegate.getLocalPathName();
	}

	/**
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#toVfsUrl()
	 */
	public URL toVfsUrl() throws MalformedURLException, URISyntaxException
	{

		return delegate.toVfsUrl();
	}

	/**
	 * @throws IOException
	 * @throws URISyntaxException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#getRealURL()
	 */
	public URL getRealURL() throws IOException, URISyntaxException
	{

		return delegate.getRealURL();
	}

	/**
	 * @throws URISyntaxException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#toURI()
	 */
	public URI toURI() throws URISyntaxException
	{

		return delegate.toURI();
	}

	/**
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#toURL()
	 */
	public URL toURL() throws MalformedURLException, URISyntaxException
	{

		return delegate.toURL();
	}

	/**
	 * @throws IOException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#getLastModified()
	 */
	public long getLastModified() throws IOException
	{

		return delegate.getLastModified();
	}

	/**
	 * @throws IOException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#hasBeenModified()
	 */
	public boolean hasBeenModified() throws IOException
	{

		return delegate.hasBeenModified();
	}

	/**
	 * @throws IOException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#getSize()
	 */
	public long getSize() throws IOException
	{

		return delegate.getSize();
	}

	/**
	 * @throws IOException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#exists()
	 */
	public boolean exists() throws IOException
	{

		return delegate.exists();
	}

	/**
	 * @throws IOException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#isLeaf()
	 */
	public boolean isLeaf() throws IOException
	{

		return delegate.isLeaf();
	}

	/**
	 * @throws IOException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#isHidden()
	 */
	public boolean isHidden() throws IOException
	{

		return delegate.isHidden();
	}

	/**
	 * @throws IOException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#openStream()
	 */
	public InputStream openStream() throws IOException
	{

		return delegate.openStream();
	}

	/**
	 * @throws IOException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#getParent()
	 */
	public VirtualFileHandler getParent() throws IOException
	{

		return delegate.getParent();
	}

	/**
	 * @param ignoreErrors
	 * @throws IOException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#getChildren(boolean)
	 */
	public List<VirtualFileHandler> getChildren(boolean ignoreErrors) throws IOException
	{

		return delegate.getChildren(ignoreErrors);
	}

	/**
	 * @param name
	 * @throws IOException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#removeChild(java.lang.String)
	 */
	public boolean removeChild(String name) throws IOException
	{

		return delegate.removeChild(name);
	}

	/**
	 * @see org.jboss.virtual.spi.VirtualFileHandler#getVFSContext()
	 */
	public VFSContext getVFSContext()
	{

		return delegate.getVFSContext();
	}

	/**
	 * @see org.jboss.virtual.spi.VirtualFileHandler#getVirtualFile()
	 */
	public VirtualFile getVirtualFile()
	{

		return new VirtualFile(this);
	}

	/**
	 * 
	 * @see org.jboss.virtual.spi.VirtualFileHandler#close()
	 */
	public void close()
	{

		delegate.close();
	}

	/**
	 * @param original
	 * @param replacement
	 * @see org.jboss.virtual.spi.VirtualFileHandler#replaceChild(org.jboss.virtual.spi.VirtualFileHandler,
	 *      org.jboss.virtual.spi.VirtualFileHandler)
	 */
	public void replaceChild(VirtualFileHandler original, VirtualFileHandler replacement)
	{

		delegate.replaceChild(original, replacement);
	}

	/**
	 * @throws IOException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#isNested()
	 */
	public boolean isNested() throws IOException
	{

		return delegate.isNested();
	}

	/**
	 * @param gracePeriod
	 * @throws IOException
	 * @see org.jboss.virtual.spi.VirtualFileHandler#delete(int)
	 */
	public boolean delete(int gracePeriod) throws IOException
	{

		return delegate.delete(gracePeriod);
	}

	/**
	 * @see org.jboss.virtual.spi.VirtualFileHandler#cleanup()
	 */
	public void cleanup()
	{

		delegate.cleanup();
	}

	/**
	 * @see org.jboss.virtual.spi.VirtualFileHandler#isArchive()
	 */
	public boolean isArchive() throws IOException
	{

		return delegate.isArchive();
	}

	/**
	 * @return the delegate
	 */
	public VirtualFileHandler getDelegate()
	{

		return delegate;
	}

}