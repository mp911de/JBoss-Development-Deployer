package de.paluch.jboss;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.deployers.spi.DeploymentException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.paluch.jboss.model.DevelopmentEntry;
import de.paluch.jboss.model.DevelopmentStructure;
import de.paluch.jboss.model.WebAppDevelopmentEntry;

/**
 * JBoss Development Deployer Parser. <br>
 * <br>
 * Projekt: jboss-dev-deployer <br>
 * Autor: mark <br>
 * Last Change: <br>
 * $Id:$ <br>
 * <br>
 * Copyright (c): Mark Paluch 2011 <br>
 */
public class JBossDevelopmentParser
{

	public DevelopmentStructure parseXml(InputStream is) throws DeploymentException
	{

		try
		{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(is);

			DevelopmentStructure structure = new DevelopmentStructure();

			List<Node> nodes = getNodes(document);
			int counter = 0;
			for (Node node : nodes)
			{

				Node idNode = node.getAttributes().getNamedItem("id");
				String path = node.getTextContent();
				if (path != null)
				{
					String id = "" + counter++;
					if (idNode != null && idNode.getTextContent() != null)
					{
						id = idNode.getTextContent();
					}

					DevelopmentEntry entry = null;

					if (node.getNodeName().equalsIgnoreCase("webapp-path"))
					{
						entry = new WebAppDevelopmentEntry(id, path);
					}
					else
					{
						entry = new DevelopmentEntry(id, path);
					}

					structure.getFileNameMap().put(id, entry);
					structure.getDevelopmentEntries().add(entry);
				}
			}

			return structure;
		}
		catch (ParserConfigurationException e)
		{
			throw new DeploymentException(e);
		}
		catch (SAXException e)
		{
			throw new DeploymentException(e);
		}
		catch (IOException e)
		{
			throw new DeploymentException(e);
		}
	}
	private List<Node> getNodes(Document document)
	{

		List<Node> nodes = new ArrayList<Node>();

		addNodes(nodes, document.getElementsByTagName("webapp-path"));
		addNodes(nodes, document.getElementsByTagName("classpath"));

		return nodes;

	}
	/**
	 * @param nodes
	 * @param list
	 */
	private void addNodes(List<Node> nodes, NodeList list)
	{

		for (int i = 0; i < list.getLength(); i++)
		{
			nodes.add(list.item(i));
		}
	}

}
