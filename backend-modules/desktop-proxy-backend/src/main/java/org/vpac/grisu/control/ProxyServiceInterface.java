package org.vpac.grisu.control;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.ws.rs.Path;
import javax.xml.ws.soap.MTOM;

import org.vpac.grisu.control.exceptions.BatchJobException;
import org.vpac.grisu.control.exceptions.JobPropertiesException;
import org.vpac.grisu.control.exceptions.JobSubmissionException;
import org.vpac.grisu.control.exceptions.NoSuchJobException;
import org.vpac.grisu.control.exceptions.NoSuchTemplateException;
import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
import org.vpac.grisu.frontend.control.login.LoginException;
import org.vpac.grisu.frontend.control.login.LoginManager;
import org.vpac.grisu.model.MountPoint;
import org.vpac.grisu.model.dto.DtoActionStatus;
import org.vpac.grisu.model.dto.DtoApplicationDetails;
import org.vpac.grisu.model.dto.DtoApplicationInfo;
import org.vpac.grisu.model.dto.DtoBatchJob;
import org.vpac.grisu.model.dto.DtoDataLocations;
import org.vpac.grisu.model.dto.DtoFolder;
import org.vpac.grisu.model.dto.DtoGridResources;
import org.vpac.grisu.model.dto.DtoHostsInfo;
import org.vpac.grisu.model.dto.DtoJob;
import org.vpac.grisu.model.dto.DtoJobs;
import org.vpac.grisu.model.dto.DtoMountPoints;
import org.vpac.grisu.model.dto.DtoProperties;
import org.vpac.grisu.model.dto.DtoStringList;
import org.vpac.grisu.model.dto.DtoSubmissionLocations;

@Path("/grisu")
@WebService(endpointInterface = "org.vpac.grisu.control.ServiceInterface")
@MTOM(enabled = true)
// @StreamingAttachment(parseEagerly = true, memoryThreshold = 40000L)
public class ProxyServiceInterface implements ServiceInterface {

	private ServiceInterface si = null;

	public void addJobProperties(String jobname, DtoJob properties)
			throws NoSuchJobException {
		si.addJobProperties(jobname, properties);

	}

	public void addJobProperty(String jobname, String key, String value)
			throws NoSuchJobException {
		si.addJobProperty(jobname, key, value);
	}

	public String addJobToBatchJob(String batchjobname, String jobdescription)
			throws NoSuchJobException, JobPropertiesException {
		return si.addJobToBatchJob(batchjobname, jobdescription);
	}

	public String archiveJob(String jobname, String target)
			throws NoSuchJobException, JobPropertiesException,
			RemoteFileSystemException {
		return si.archiveJob(jobname, target);
	}

	public void copyBatchJobInputFile(String batchJobname, String inputFile,
			String filename) throws RemoteFileSystemException,
			NoSuchJobException {
		si.copyBatchJobInputFile(batchJobname, inputFile, filename);
	}

	public String cp(DtoStringList sources, String target, boolean overwrite,
			boolean wait) throws RemoteFileSystemException {
		return si.cp(sources, target, overwrite, wait);
	}

	public DtoBatchJob createBatchJob(String batchJobname, String fqan)
			throws BatchJobException {
		return si.createBatchJob(batchJobname, fqan);
	}

	public String createJob(String jsdl, String fqan,
			String jobnameCreationMethod) throws JobPropertiesException {
		return si.createJob(jsdl, fqan, jobnameCreationMethod);
	}

	public void deleteFile(String url) throws RemoteFileSystemException {
		si.deleteFile(url);
	}

	public void deleteFiles(DtoStringList files) {
		si.deleteFiles(files);
	}

	public DtoMountPoints df() {
		return si.df();
	}

	public DataHandler download(String filename)
			throws RemoteFileSystemException {
		return si.download(filename);
	}

	public boolean fileExists(String file) throws RemoteFileSystemException {
		return si.fileExists(file);
	}

	public DtoGridResources findMatchingSubmissionLocationsUsingJsdl(
			String jsdl, String fqan, boolean exclude) {
		return si.findMatchingSubmissionLocationsUsingJsdl(jsdl, fqan, exclude);
	}

	public DtoGridResources findMatchingSubmissionLocationsUsingMap(
			DtoJob jobProperties, String fqan, boolean exclude) {
		return si.findMatchingSubmissionLocationsUsingMap(jobProperties, fqan,
				exclude);
	}

	public DtoActionStatus getActionStatus(String actionHandle) {
		return si.getActionStatus(actionHandle);
	}

	public DtoStringList getAllAvailableApplications(DtoStringList sites) {
		return si.getAllAvailableApplications(sites);
	}

	public DtoStringList getAllBatchJobnames(String application) {
		return si.getAllBatchJobnames(application);
	}

	public DtoHostsInfo getAllHosts() {
		return si.getAllHosts();
	}

	public DtoStringList getAllJobnames(String application) {
		return si.getAllJobnames(application);
	}

	public DtoStringList getAllSites() {
		return si.getAllSites();
	}

	public DtoSubmissionLocations getAllSubmissionLocations() {
		return si.getAllSubmissionLocations();
	}

	public DtoSubmissionLocations getAllSubmissionLocationsForFqan(String fqan) {
		return si.getAllSubmissionLocationsForFqan(fqan);
	}

	public DtoApplicationDetails getApplicationDetailsForVersionAndSubmissionLocation(
			String application, String version, String site) {
		return si.getApplicationDetailsForVersionAndSubmissionLocation(
				application, version, site);
	}

	public String[] getApplicationPackagesForExecutable(String executable) {
		return si.getApplicationPackagesForExecutable(executable);
	}

	public DtoBatchJob getBatchJob(String batchJobname)
			throws NoSuchJobException {
		return si.getBatchJob(batchJobname);
	}

	public DtoProperties getBookmarks() {
		return si.getBookmarks();
	}

	public DtoStringList getChildrenFileNames(String url, boolean onlyFiles)
			throws RemoteFileSystemException {
		return si.getChildrenFileNames(url, onlyFiles);
	}

	public long getCredentialEndTime() {
		return si.getCredentialEndTime();
	}

	public DtoDataLocations getDataLocationsForVO(String fqan) {
		return si.getDataLocationsForVO(fqan);
	}

	public String getDN() {
		return si.getDN();
	}

	public long getFileSize(String url) throws RemoteFileSystemException {
		return si.getFileSize(url);
	}

	public DtoStringList getFqans() {
		return si.getFqans();
	}

	public String getInterfaceInfo(String key) {
		return si.getInterfaceInfo(key);
	}

	public DtoJob getJob(String jobname) throws NoSuchJobException {
		return si.getJob(jobname);
	}

	public String getJobProperty(String jobname, String key)
			throws NoSuchJobException {
		return si.getJobProperty(jobname, key);
	}

	public int getJobStatus(String jobname) {
		return si.getJobStatus(jobname);
	}

	public String getJsdlDocument(String jobname) throws NoSuchJobException {
		return si.getJsdlDocument(jobname);
	}

	public MountPoint getMountPointForUri(String url) {
		return si.getMountPointForUri(url);
	}

	public String getSite(String host) {
		return si.getSite(host);
	}

	public DtoStringList getStagingFileSystemForSubmissionLocation(String subLoc) {
		return si.getStagingFileSystemForSubmissionLocation(subLoc);
	}

	public DtoSubmissionLocations getSubmissionLocationsForApplication(
			String application) {
		return si.getSubmissionLocationsForApplication(application);
	}

	public DtoSubmissionLocations getSubmissionLocationsForApplicationAndVersion(
			String application, String version) {
		return si.getSubmissionLocationsForApplicationAndVersion(application,
				version);
	}

	public DtoSubmissionLocations getSubmissionLocationsForApplicationAndVersionAndFqan(
			String application, String version, String fqan) {
		return si.getSubmissionLocationsForApplicationAndVersionAndFqan(
				application, version, fqan);
	}

	public DtoApplicationInfo getSubmissionLocationsPerVersionOfApplication(
			String application) {
		return si.getSubmissionLocationsPerVersionOfApplication(application);
	}

	public String getTemplate(String application)
			throws NoSuchTemplateException {
		return si.getTemplate(application);
	}

	public DtoStringList getUsedApplications() {
		return si.getUsedApplications();
	}

	public DtoStringList getUsedApplicationsBatch() {
		return si.getUsedApplicationsBatch();
	}

	public DtoProperties getUserProperties() {
		return si.getUserProperties();
	}

	public String getUserProperty(String key) {
		return si.getUserProperty(key);
	}

	public DtoStringList getVersionsOfApplicationOnSubmissionLocation(
			String application, String submissionLocation) {
		return si.getVersionsOfApplicationOnSubmissionLocation(application,
				submissionLocation);
	}

	public boolean isFolder(String file) throws RemoteFileSystemException {
		return si.isFolder(file);
	}

	public void kill(String jobname, boolean clean)
			throws RemoteFileSystemException, NoSuchJobException,
			BatchJobException {
		si.kill(jobname, clean);
	}

	public void killJobs(DtoStringList jobnames, boolean clean) {
		si.killJobs(jobnames, clean);
	}

	public long lastModified(String url) throws RemoteFileSystemException {
		return si.lastModified(url);
	}

	public String[] listHostedApplicationTemplates() {
		return si.listHostedApplicationTemplates();
	}

	public void login(String username, String password) {

		System.out
				.println("Username: " + username + " / password: " + password);

		try {
			si = LoginManager.myProxyLogin("Local", username,
					password.toCharArray());
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String logout() {

		return null;
		// String result = si.logout();
		// si = null;
		// return result;
	}

	public DtoFolder ls(String url, int recursionLevel)
			throws RemoteFileSystemException {
		return si.ls(url, recursionLevel);
	}

	public boolean mkdir(String url) throws RemoteFileSystemException {
		return si.mkdir(url);
	}

	public MountPoint mount(String url, String alias, String fqan,
			boolean useHomeDirectoryOnThisFileSystemIfPossible)
			throws RemoteFileSystemException {
		return si.mount(url, alias, fqan,
				useHomeDirectoryOnThisFileSystemIfPossible);
	}

	public MountPoint mountWithoutFqan(String url, String alias,
			boolean useHomeDirectoryOnThisFileSystemIfPossible)
			throws RemoteFileSystemException {
		return si.mountWithoutFqan(url, alias,
				useHomeDirectoryOnThisFileSystemIfPossible);
	}

	public DtoJobs ps(String application, boolean refreshJobStatus) {
		return si.ps(application, refreshJobStatus);
	}

	public String redistributeBatchJob(String batchjobname)
			throws NoSuchJobException, JobPropertiesException {
		return si.redistributeBatchJob(batchjobname);
	}

	public String refreshBatchJobStatus(String batchJobname)
			throws NoSuchJobException {
		return si.refreshBatchJobStatus(batchJobname);
	}

	public void removeJobFromBatchJob(String batchJobname, String jobname)
			throws NoSuchJobException {
		si.removeJobFromBatchJob(batchJobname, jobname);
	}

	public DtoProperties restartBatchJob(String jobname, String restartPolicy,
			DtoProperties properties) throws NoSuchJobException,
			JobPropertiesException {
		return si.restartBatchJob(jobname, restartPolicy, properties);
	}

	public void restartJob(String jobname, String changedJsdl)
			throws JobSubmissionException, NoSuchJobException {
		si.restartJob(jobname, changedJsdl);
	}

	public void setBookmark(String alias, String value) {
		si.setBookmark(alias, value);
	}

	public void setUserProperty(String key, String value) {
		si.setUserProperty(key, value);
	}

	public void submitJob(String jobname) throws JobSubmissionException,
			NoSuchJobException {
		si.submitJob(jobname);
	}

	public void submitSupportRequest(String subject, String description) {
		si.submitSupportRequest(subject, description);
	}

	public void umount(String alias) {
		si.umount(alias);
	}

	public String upload(DataHandler file, String filename)
			throws RemoteFileSystemException {
		return si.upload(file, filename);
	}

	public void uploadInputFile(String jobname, DataHandler inputFile,
			String relativePath) throws RemoteFileSystemException,
			NoSuchJobException {
		si.uploadInputFile(jobname, inputFile, relativePath);
	}

}
