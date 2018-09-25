package com.ufhoneybee.MavenLib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.HashMap;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


 
/**
 * @author Hubert Zhao
 * to make check for and download Plugins
 * made for Honeybee Research Lab
 * code to download files was made by www.codejava.net, http://www.codejava.net/java-se/networking/use-httpurlconnection-to-download-file-from-an-http-url
 *
 */


public class MavenLib {
	
	
	/*
		//check workspace for jar file,
		
		
			//if not there then download plugin with pom into workspace, check pom
				//if dependencies then check workspace for dependencies
				//if no dependencies finish
			
			//if there then check pom
				//if dependencies then check workspace for dependencies
				//if no dependencies finish
		
		
		return null;
	*/
	
	public static LinkedList<String> createDirectory(String pluginFilePath, String rootGroupID, String rootArtifactID, String rootVersion) throws DownloadFailedException {
		String sep = System.getProperty("file.separator");
		LinkedList<String> dependencies = new LinkedList<String>();
		Deque<String> dependencyDeque = new ArrayDeque<String>();
		HashMap<String, ArrayList<String>> dependencyMap = new HashMap<>();
		ArrayList<ArrayList<String>> deleteFileTracker = new ArrayList<>();
		HashMap<String, ArrayList<String>> dependencyIdentifiers = new HashMap<>();
		dependencyDeque.add("groupID:" + rootGroupID + "/artifactID:" + rootArtifactID + "/version:" + rootVersion + "/");
		
		File pluginFolder = new File(pluginFilePath);
		if (!pluginFolder.exists()) {
			pluginFolder.mkdirs();
			System.out.println("New Plugin folder created");
		}
		boolean DeleteAll = false;
		// 0 means nothing new
		// 1 means new group folder
		// 2 means new artifact folder
		// 3 means new version
	
		
		
		while (!dependencyDeque.isEmpty() && !DeleteAll) {
			int stageToDelete = -1;
			ArrayList<String> arrayToAdd = new ArrayList<>();
			
			String fullID = dependencyDeque.poll();
			String groupID = fullID.substring(fullID.indexOf("groupID:") + 8, fullID.indexOf(('/'), fullID.indexOf("groupID:")));
			String artifactID = fullID.substring(fullID.indexOf("artifactID:") + 11, fullID.indexOf(('/'), fullID.indexOf("artifactID:")));
			String version = fullID.substring(fullID.indexOf("version:") + 8, fullID.indexOf(('/'), fullID.indexOf("version:")));
			
			//check if plugin exists
			boolean pluginJarExists = false;
			boolean pluginPomExists = false;                                                                                                                                                                                
			
			File pluginGroupFolder = new File(pluginFilePath + sep + groupID);
			if (!pluginGroupFolder.exists()) {
				pluginGroupFolder.mkdirs();
				System.out.println("New GroupID folder created");
				
				stageToDelete = 1;
			}
			arrayToAdd.add(groupID);
			
			File pluginArtifactFolder = new File(pluginFilePath + sep + groupID + sep + artifactID);
			if (!pluginArtifactFolder.exists()) {
				pluginArtifactFolder.mkdirs();
				System.out.println("New ArtifactID folder created");
				
				
			}
			if (stageToDelete == -1) {
				arrayToAdd.add(artifactID);
				stageToDelete = 2;
				
			}
			
			File pluginVersionFolder = new File(pluginFilePath + sep + groupID + sep + artifactID + sep + version);
			if (!pluginVersionFolder.exists()) {
				pluginVersionFolder.mkdirs();
				System.out.println("New Version folder created");
				
				
				
			}
			
			if (stageToDelete == -1) {
				arrayToAdd.add(version);
				stageToDelete = 3;
				
			}
			
			File pluginJarFile = new File(pluginFilePath + sep + groupID + sep + artifactID + sep + version + sep + artifactID + "-" + version + ".jar");
			if (!pluginJarFile.exists() && !dependencies.contains(pluginFilePath + sep + groupID + sep + artifactID + sep + version + sep + artifactID + "-" + version + ".jar")) {
				pluginJarExists = false; //the && above makes sure list doesn't contain duplicates, referring to next comment
				System.out.println("Plugin Jar File does not exist, downloading next");
			}
			else {
				pluginJarExists = true; //or it could've had an error downloading...
				if (dependencies.contains(pluginFilePath + sep + groupID + sep + artifactID + sep + version + sep + artifactID + "-" + version + ".jar")) {
					dependencies.remove(pluginFilePath + sep + groupID + sep + artifactID + sep + version + sep + artifactID + "-" + version + ".jar");
					dependencies.add(pluginFilePath + sep + groupID + sep + artifactID + sep + version + sep + artifactID + "-" + version + ".jar");
					
					
				}
				else {
					dependencies.add(pluginFilePath + sep + groupID + sep + artifactID + sep + version + sep + artifactID + "-" + version + ".jar");
				}
				stageToDelete = 0;
			
				System.out.println("Plugin Jar File exists");
			}
			
			File pluginPomFile = new File(pluginFilePath + sep + groupID + sep + artifactID + sep + version + sep + artifactID + "-" + version + ".pom");
			if (!pluginPomFile.exists()) {
				pluginPomExists = false;
				System.out.println("Plugin Pom File does not exist, downloading next");
			}
			else {
				pluginPomExists = true;
				System.out.println("Plugin Pom File exists");
			}
			
			
			if (stageToDelete > 0) {
				deleteFileTracker.add(arrayToAdd);
			}
			
			//downloading files if needed
			if (!pluginJarExists) {
				
				String downloadGroupID;
				try {
					if (!groupID.contains(".")) {
						String pluginJarURL = "https://repo1.maven.org/maven2/" + groupID + "/" + artifactID + "/" + version + "/" + artifactID + "-" + version + ".jar";
						downloadFile(pluginJarURL, pluginFilePath + sep + groupID + sep + artifactID + sep + version, artifactID + "-" + version + ".jar");
					}
					else {
						downloadGroupID = groupID;
						while (downloadGroupID.contains(".")) {
				
							int nextIndex = downloadGroupID.indexOf('.');
							
							if (nextIndex + 1 < downloadGroupID.length()) {
								downloadGroupID = downloadGroupID.substring(0, nextIndex) + "/" + downloadGroupID.substring(nextIndex + 1, downloadGroupID.length());
							}
							else {
								downloadGroupID = downloadGroupID.substring(0, nextIndex) + "/";
							}
						}
						System.out.println("downloadGroupID: " + downloadGroupID);
						String pluginJarURL = "https://repo1.maven.org/maven2/" + downloadGroupID + "/" + artifactID + "/" + version + "/" + artifactID + "-" + version + ".jar";
					
						downloadFile(pluginJarURL, pluginFilePath + sep + groupID + sep + artifactID + sep + version, artifactID + "-" + version + ".jar");
					}
					dependencies.add(pluginFilePath + sep + groupID + sep + artifactID + sep + version + sep + artifactID + "-" + version + ".jar");
					System.out.println("DEPENDENCY ADDED " + pluginFilePath + sep + groupID + sep + artifactID + sep + version + sep + artifactID + "-" + version + ".jar");
					
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
					//throw new DownloadFailedException("Download Failed because Jar File Does Not Exist");
					DeleteAll = true;
					break;
					
				}
				catch (DownloadFailedException ex) {
					ex.printStackTrace();
					DeleteAll = true;
					break;
					
				}
				
				
				
			}
			
			
			//dependencyMap.put(arrayToAdd, arrayToAdd);
			
			
			
			if (!pluginPomExists) {
				
				String downloadGroupID;
				try {
					if (!groupID.contains(".")) {
						String pluginPomURL = "https://repo1.maven.org/maven2/" + groupID + "/" + artifactID + "/" + version + "/" + artifactID + "-" + version + ".pom";
						downloadFile(pluginPomURL, pluginFilePath + sep + groupID + sep + artifactID + sep + version, artifactID + "-" + version + ".pom");
					}
					else {
						downloadGroupID = groupID;
						while (downloadGroupID.contains(".")) {
				
							int nextIndex = downloadGroupID.indexOf('.');
							
							if (nextIndex + 1 < downloadGroupID.length()) {
								downloadGroupID = downloadGroupID.substring(0, nextIndex) + "/" + downloadGroupID.substring(nextIndex + 1, downloadGroupID.length());
							}
							else {
								downloadGroupID = downloadGroupID.substring(0, nextIndex) + "/";
							}
						}
						System.out.println("downloadGroupID: " + downloadGroupID);
						String pluginPomURL = "https://repo1.maven.org/maven2/" + downloadGroupID + "/" + artifactID + "/" + version + "/" + artifactID + "-" + version + ".pom";
					
						downloadFile(pluginPomURL, pluginFilePath + sep + groupID + sep + artifactID + sep + version, artifactID + "-" + version + ".pom");
					}
				
					
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
					DeleteAll = true;
					break;
					//throw new DownloadFailedException("Download Failed because Pom File Does Not Exist");
				}
				catch (DownloadFailedException ex) {
					ex.printStackTrace();
					DeleteAll = true;
					break;
					
				}
				
			}
			
			
			
			
			
			
			//checking pom file
		
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(pluginPomFile);
				NodeList dependencyList = doc.getElementsByTagName("dependency");
				
				for (int i = 0; i < dependencyList.getLength(); i++) {
					Node d = dependencyList.item(i);
					if (d.getNodeType() == Node.ELEMENT_NODE) {
						Element dependency = (Element) d;
						NodeList dependencyIDList = dependency.getChildNodes();
						
						String IDcode = null;
						
						for (int j = 0; j < dependencyIDList.getLength(); j++) {
							Node ID = dependencyIDList.item(j);
							
							if (ID.getNodeType() == Node.ELEMENT_NODE) {
								
								Element IDName = (Element) ID;
								
								if (IDName.getTagName() == "groupId") {
									
									IDcode += "groupID:" + (String)IDName.getTextContent() + "/";
								
									
								}
								if (IDName.getTagName() == "artifactId") {
									IDcode +=  "artifactID:" + (String)IDName.getTextContent() + "/";
									if (((String)(IDName.getTextContent())).contains("\n")) {
										System.out.println("contains endline 2" + " artifactID: " + (String)IDName.getTextContent());
									}
								}
								
								if (IDName.getTagName() == "version") {
									IDcode += "version:" + (String)IDName.getTextContent() + "/";
									
								}
							}
							
						}
						if (IDcode != null) {
							//if (!dependencies.contains(check + IDcode.substring(IDcode.indexOf("artifactID:") + 11, IDcode.indexOf("/",IDcode.indexOf("artifactID:"))) + "-" + IDcode.substring(IDcode.indexOf("version:") + 8, IDcode.indexOf("/",IDcode.indexOf("version:"))) + ".jar")) {
								dependencyDeque.add(IDcode);
							//}
					
						}
								
					}
				}
				
			} catch (ParserConfigurationException ex) {
				System.out.println(pluginFilePath + sep + groupID + sep + artifactID + sep + version + sep + artifactID + "-" + version + ".jar1 ERROR");
				ex.printStackTrace();
			} catch (SAXException ex) {
				System.out.println(pluginFilePath + sep + groupID + sep + artifactID + sep + version + sep + artifactID + "-" + version + ".jar2 ERROR");
				ex.printStackTrace();
			} catch (IOException ex) {
				System.out.println(pluginFilePath + sep + groupID + sep + artifactID + sep + version + sep + artifactID + "-" + version + ".jar3 ERROR");
				ex.printStackTrace();
			}
			
		}
		
		
		
		if (DeleteAll) {
			
			while (!deleteFileTracker.isEmpty()) {
				String deleteFilePath = pluginFilePath;
				ArrayList<String> fileOne = deleteFileTracker.remove(0);
				
				for (int i = 0; i < fileOne.size(); i++) {
					deleteFilePath += sep + fileOne.get(i);
				}
				System.out.println("deleteFilePath: "  + deleteFilePath);
				File deleteFile = new File(deleteFilePath);
				deleteFileMethod(deleteFile);
				
				
			}
			dependencies.clear();
		}
		
		
		return dependencies;
		
	}
	
	
	//if deleteAll...
	
	public static void deleteFileMethod(File file) {
        
        File[] fileArray = file.listFiles();
        for (int i = 0; i < fileArray.length;i++) {
            deleteFileMethod(fileArray[i]);
            System.out.println("deleting working yay!");
        }
        System.out.println("deleting working yay!");
        file.delete();
        

    }
	
	
	
	
	private static final int BUFFER_SIZE = 4096;
 
    /**
     * Downloads a file from a URL
     * @param fileURL HTTP URL of the file to be downloaded
     * @param saveDir path of the directory to save the file
     * @throws IOException
     */
    public static void downloadFile(String fileURL, String saveDir, String fileName)
        throws IOException, DownloadFailedException {
    		System.out.println("fileURL: " + fileURL);
    		fileURL = fileURL.replace("\n", "");
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
        int responseCode = httpConn.getResponseCode();
 
        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            
            System.out.println("fileName = " + fileName);
 
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;
             
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
 
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            outputStream.close();
            inputStream.close();
 
            System.out.println("File downloaded");
        } 
        else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
            throw new DownloadFailedException("Failed to download: File not found");
        }
        httpConn.disconnect();
    }
}

//handle exclusions
//

