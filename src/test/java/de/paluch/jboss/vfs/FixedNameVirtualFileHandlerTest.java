package de.paluch.jboss.vfs;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.jboss.virtual.spi.VirtualFileHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * 
 * <br>
 * <br>
 * Projekt: jboss-dev-deployer <br>
 * Autor: mark <br>
 * Last Change: <br>
 * $Id:$ <br>
 * <br>
 * Copyright (c): Mark Paluch 2011 <br>
 */
@RunWith(MockitoJUnitRunner.class)
public class FixedNameVirtualFileHandlerTest
{

	@Mock
	private VirtualFileHandler delegate;

	private FixedNameVirtualFileHandler handler;

	@Before
	public void setup()
	{

		handler = new FixedNameVirtualFileHandler(delegate, null, null, null);
	}

	/**
	 * Test method for
	 * {@link de.paluch.jboss.vfs.FixedNameVirtualFileHandler#getName()}.
	 */
	@Test
	public void testGetName()
	{

		handler.getName();
		Mockito.verify(delegate).getName();
	}

	/**
	 * Test method for
	 * {@link de.paluch.jboss.vfs.FixedNameVirtualFileHandler#getPathName()}.
	 */
	@Test
	public void testGetPathName()
	{

		handler.getPathName();
		Mockito.verify(delegate).getPathName();
	}

	/**
	 * Test method for
	 * {@link de.paluch.jboss.vfs.FixedNameVirtualFileHandler#getName()}.
	 */
	@Test
	public void testGetNameFixed()
	{

		handler = new FixedNameVirtualFileHandler(delegate, null, null, "name");
		String result = handler.getName();

		assertThat(result, is("name"));
		Mockito.verify(delegate, Mockito.times(0)).getName();
	}

	/**
	 * Test method for
	 * {@link de.paluch.jboss.vfs.FixedNameVirtualFileHandler#getPathName()}.
	 */
	@Test
	public void testGetPathNameFixed()
	{

		handler = new FixedNameVirtualFileHandler(delegate, null, "path", null);
		String result = handler.getPathName();

		assertThat(result, is("path"));
		Mockito.verify(delegate, Mockito.times(0)).getPathName();
	}

	@Test
	public void testGetParent() throws IOException
	{

		handler = new FixedNameVirtualFileHandler(delegate, delegate, "path", null);
		VirtualFileHandler result = handler.getParent();

		assertThat(result, is(delegate));
	}

}
