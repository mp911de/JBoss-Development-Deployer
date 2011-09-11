package de.paluch.jboss;

import java.io.IOException;
import java.net.URL;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.virtual.VirtualFile;
import org.jboss.web.tomcat.service.deployers.TomcatDeployer;

/**
 * Development Tomcat-Deployer. Workarround for JBoss 5 when using Development
 * Redirection. <br>
 * <br>
 * Projekt: jboss-dev-deployer <br>
 * Autor: mark <br>
 * Last Change: <br>
 * $Id$ <br>
 * <br>
 */
public class DevelopmentTomcatDeployer extends TomcatDeployer
{

	/**
	 * removes expanded*-Attachments from DeploymentUnit.
	 * 
	 * @see org.jboss.web.deployers.AbstractWarDeployer#deploy(org.jboss.deployers.structure.spi.DeploymentUnit,
	 *      org.jboss.metadata.web.jboss.JBossWebMetaData)
	 */
	@Override
	public void deploy(DeploymentUnit unit, JBossWebMetaData metaData) throws DeploymentException
	{

		super.deploy(unit, metaData);

		if (unit instanceof VFSDeploymentUnit)
		{

			VFSDeploymentUnit vfsUnit = (VFSDeploymentUnit) unit;
			try
			{
				VirtualFile development = vfsUnit.getRoot().getChild("WEB-INF/jboss-development.xml");

				if (development != null && development.exists())
				{
					// Remove Attachments (JBoss5 Bug)
					unit.removeAttachment("org.jboss.web.expandedWarURL", URL.class);
					unit.removeAttachment("org.jboss.web.expandedWarFile", VirtualFile.class);
				}

			}
			catch (IOException e)
			{
				throw new DeploymentException(e);
			}

		}

	}
}
