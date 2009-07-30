package org.vpac.grisu.control;

import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebService;

import org.vpac.grisu.control.exceptions.JobPropertiesException;
import org.vpac.grisu.control.exceptions.JobSubmissionException;
import org.vpac.grisu.control.exceptions.NoSuchJobException;
import org.vpac.grisu.control.exceptions.NoSuchTemplateException;
import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
import org.vpac.grisu.model.MountPoint;
import org.vpac.grisu.model.dto.DtoApplicationDetails;
import org.vpac.grisu.model.dto.DtoApplicationInfo;
import org.vpac.grisu.model.dto.DtoDataLocations;
import org.vpac.grisu.model.dto.DtoFolder;
import org.vpac.grisu.model.dto.DtoGridResources;
import org.vpac.grisu.model.dto.DtoHostsInfo;
import org.vpac.grisu.model.dto.DtoJob;
import org.vpac.grisu.model.dto.DtoJobs;
import org.vpac.grisu.model.dto.DtoMountPoints;
import org.vpac.grisu.model.dto.DtoSubmissionLocations;

/**
   XFire version of ServiceInterface
 */
@WebService
public interface XFireServiceInterface extends ServiceInterface{
	
	@WebMethod
	String getInterfaceVersion();
	@WebMethod
	void login(String username, String password);
	@WebMethod
	String logout();
	@WebMethod
	String[] listHostedApplicationTemplates();
	@WebMethod
	String getTemplate(String application)
			throws NoSuchTemplateException;

	@WebMethod
	void submitSupportRequest(String subject, String description);
	@WebMethod
	String getUserProperty(String key);
	@WebMethod
	long getCredentialEndTime();
	@WebMethod
	String getCurrentStatusMessage(String handle);
	@WebMethod
	String getSite(String host);
	@WebMethod
	DtoHostsInfo getAllHosts();
	@WebMethod
	DtoSubmissionLocations getAllSubmissionLocations();
	@WebMethod
	DtoSubmissionLocations getAllSubmissionLocationsForFqan(String fqan);
	@WebMethod
	DtoSubmissionLocations getSubmissionLocationsForApplication(String application);
	@WebMethod
	DtoSubmissionLocations getSubmissionLocationsForApplicationAndVersion(String application,
			String version);
	@WebMethod
	DtoSubmissionLocations getSubmissionLocationsForApplicationAndVersionAndFqan(String application,
			String version, String fqan);
	@WebMethod
	DtoApplicationInfo getSubmissionLocationsPerVersionOfApplication(
			String application);
	@WebMethod
	DtoDataLocations getDataLocationsForVO(String fqan);
	@WebMethod
	String[] getVersionsOfApplicationOnSubmissionLocation(
			String application, String submissionLocation);
	@WebMethod
	String[] getStagingFileSystemForSubmissionLocation(String subLoc);
	@WebMethod
	String[] getFqans();
	@WebMethod
	String getDN();
	@WebMethod
	String[] getAllSites();
	@WebMethod
	String[] getAllAvailableApplications(String[] sites);
	@WebMethod
	DtoApplicationDetails getApplicationDetailsForVersionAndSite(String application,
			String version, String site);
	@WebMethod
	DtoApplicationDetails getApplicationDetailsForSite(String application,
			String site_or_submissionLocation);
	@WebMethod
	DtoGridResources findMatchingSubmissionLocationsUsingJsdl(String jsdl,
			String fqan);
	@WebMethod
	DtoGridResources findMatchingSubmissionLocationsUsingMap(
			DtoJob jobProperties, String fqan);
	@WebMethod
	MountPoint mountWithoutFqan(String url, String mountpoint,
			boolean useHomeDirectoryOnThisFileSystemIfPossible)
			throws RemoteFileSystemException;
	@WebMethod
	MountPoint mount(String url, String mountpoint, String fqan,
			boolean useHomeDirectoryOnThisFileSystemIfPossible)
			throws RemoteFileSystemException;
	@WebMethod
	void umount(String mountpoint);
	@WebMethod
	DtoMountPoints df();
	@WebMethod
	MountPoint getMountPointForUri(String uri);
	@WebMethod
	String upload(DataHandler file, String filename,
			boolean return_absolute_url) throws RemoteFileSystemException;
	@WebMethod
	DataHandler download(String filename)
			throws RemoteFileSystemException;
	@WebMethod
	DtoFolder ls(String directory, int recursion_level) throws RemoteFileSystemException;
	@WebMethod
	String cp(String source, String target, boolean overwrite,
			boolean waitForFileTransferToFinish)
			throws RemoteFileSystemException;
	@WebMethod
	boolean fileExists(String file) throws RemoteFileSystemException;
	@WebMethod
	boolean isFolder(String file) throws RemoteFileSystemException;
	@WebMethod
	String[] getChildrenFileNames(String folder, boolean onlyFiles)
			throws RemoteFileSystemException;
	@WebMethod
	long getFileSize(String file) throws RemoteFileSystemException;
	@WebMethod
	long lastModified(String remoteFile)
			throws RemoteFileSystemException;
	@WebMethod
	boolean mkdir(String folder) throws RemoteFileSystemException;
	@WebMethod
	void deleteFile(String file) throws RemoteFileSystemException;
	@WebMethod
	void deleteFiles(String[] files) throws RemoteFileSystemException;
	@WebMethod
	DtoJobs ps(boolean refreshJobStatus);
	@WebMethod
	String[] getAllJobnames();
	@WebMethod
	String createJobUsingMap(DtoJob job, String fqan,
			String jobnameCreationMethod) throws JobPropertiesException;
	@WebMethod
	String createJobUsingJsdl(String jsdl, String fqan,
			String jobnameCreationMethod) throws JobPropertiesException;
	@WebMethod
	void submitJob(String jobname) throws JobSubmissionException;
	@WebMethod
	int getJobStatus(String jobname);
	@WebMethod
	void kill(String jobname, boolean clean)
			throws RemoteFileSystemException, NoSuchJobException;
	@WebMethod
	void addJobProperty(String jobname, String key, String value)
			throws NoSuchJobException;
	@WebMethod
	void addJobProperties(String jobname, DtoJob properties)
			throws NoSuchJobException;
	@WebMethod
	String getJobProperty(String jobname, String key)
			throws NoSuchJobException;
	@WebMethod
	DtoJob getAllJobProperties(String jobname)
			throws NoSuchJobException;
	@WebMethod
	String getJsdlDocument(String jobname) throws NoSuchJobException;
	
}
