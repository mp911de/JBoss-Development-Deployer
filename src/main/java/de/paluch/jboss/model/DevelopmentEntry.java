package de.paluch.jboss.model;

/**
 * Development Mapping Entry. <br>
 * <br>
 * Projekt: jboss-dev-deployer <br>
 * Autor: mark <br>
 * Last Change: <br>
 * $Id: //depot/JBoss/jboss-dev-deployer/src/main/java/de/paluch/jboss/
 * DevelopmentEntry.java#2 $ <br>
 * <br>
 * Copyright (c): Mark Paluch 2011 <br>
 */
public class DevelopmentEntry
{

	private String id;
	private String name;
	private boolean used = false;

	/**
	 * @param id
	 * @param name
	 */
	public DevelopmentEntry(String id, String name)
	{

		super();
		this.id = id;
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public String getId()
	{

		return id;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{

		return name;
	}

	/**
	 * @return the used
	 */
	public boolean isUsed()
	{

		return used;
	}

	/**
	 * @param used
	 *            the used to set
	 */
	public void setUsed(boolean used)
	{

		this.used = used;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{

		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{

		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		DevelopmentEntry other = (DevelopmentEntry) obj;
		if (id == null)
		{
			if (other.id != null)
			{
				return false;
			}
		}
		else if (!id.equals(other.id))
		{
			return false;
		}
		return true;
	}

}
