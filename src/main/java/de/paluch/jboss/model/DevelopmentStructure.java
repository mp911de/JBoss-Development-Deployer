package de.paluch.jboss.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Development Data Container. <br>
 * <br>
 * Projekt: jboss-dev-deployer <br>
 * Autor: mark <br>
 * Last Change: <br>
 * $Id:$ <br>
 * <br>
 * Copyright (c): Mark Paluch 2011 <br>
 */
public class DevelopmentStructure
{

	private List<String> externalFileNames = new ArrayList<String>();
	private List<DevelopmentEntry> developmentEntries = new ArrayList<DevelopmentEntry>();
	private Map<String, DevelopmentEntry> fileNameMap = new HashMap<String, DevelopmentEntry>();

	/**
	 * @return the externalFileNames
	 */
	public List<String> getExternalFileNames()
	{

		return externalFileNames;
	}

	/**
	 * @return the developmentEntries
	 */
	public List<DevelopmentEntry> getDevelopmentEntries()
	{

		return developmentEntries;
	}

	/**
	 * @return the fileNameMap
	 */
	public Map<String, DevelopmentEntry> getFileNameMap()
	{

		return fileNameMap;
	}

	public List<WebAppDevelopmentEntry> getWebAppEntries()
	{

		List<WebAppDevelopmentEntry> result = new ArrayList<WebAppDevelopmentEntry>();
		for (DevelopmentEntry entry : developmentEntries)
		{
			if (entry instanceof WebAppDevelopmentEntry)
			{
				result.add((WebAppDevelopmentEntry) entry);
			}
		}

		return result;
	}

}
