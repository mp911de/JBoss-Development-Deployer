package de.paluch.jboss;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import org.junit.Test;

import de.paluch.jboss.model.DevelopmentEntry;
import de.paluch.jboss.model.DevelopmentStructure;
import de.paluch.jboss.model.WebAppDevelopmentEntry;

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
public class JBossDevelopmentParserTest
{

	@Test
	public void testParseWAR() throws Exception
	{

		InputStream is = getClass().getResourceAsStream("/jboss-development.war.xml");
		DevelopmentStructure structure = new JBossDevelopmentParser().parseXml(is);
		is.close();

		assertNotNull(structure);
		assertEquals(3, structure.getDevelopmentEntries().size());
		assertEquals(3, structure.getFileNameMap().size());

		DevelopmentEntry e1 = structure.getDevelopmentEntries().get(0);
		DevelopmentEntry e2 = structure.getDevelopmentEntries().get(1);
		DevelopmentEntry e3 = structure.getDevelopmentEntries().get(2);

		assertThat(e1.getId(), is("0"));
		assertThat(e1.getClass().toString(), is(WebAppDevelopmentEntry.class.toString()));
		assertThat(e1.getName(), is(equalTo("C:/Workspace/project/war/src/webapp")));

		assertThat(e2.getId(), is("1"));
		assertThat(e2.getName(), is(equalTo("C:/Workspace/project/primary-source/target/classes")));

		assertThat(e3.getId(), is("2"));
		assertThat(e3.getName(), is(equalTo("C:/Workspace/project/test.har/target/classes")));

	}

	@Test
	public void testParseEAR() throws Exception
	{

		InputStream is = getClass().getResourceAsStream("/jboss-development.ear.xml");
		DevelopmentStructure structure = new JBossDevelopmentParser().parseXml(is);
		is.close();

		assertNotNull(structure);
		assertEquals(3, structure.getDevelopmentEntries().size());
		assertEquals(3, structure.getFileNameMap().size());

		DevelopmentEntry e1 = structure.getDevelopmentEntries().get(0);
		DevelopmentEntry e2 = structure.getDevelopmentEntries().get(1);
		DevelopmentEntry e3 = structure.getDevelopmentEntries().get(2);

		assertThat(e1.getId(), is(equalTo("ejbs-1.0.jar")));
		assertThat(e1.getName(), is(equalTo("C:/Workspace/project/ejbs/target/classes")));

		assertThat(e2.getId(), is("primary-source-1.0.jar"));
		assertThat(e2.getName(), is(equalTo("C:/Workspace/project/primary-source/target/classes")));

		assertThat(e3.getId(), is("test.har-1.0.har"));
		assertThat(e3.getName(), is(equalTo("C:/Workspace/project/test.har/target/classes")));

	}

}
