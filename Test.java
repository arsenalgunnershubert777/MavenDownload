package com.ufhoneybee.MavenLib;

//import java.io.IOException;
import java.util.LinkedList;


public class Test {
	public static void main(String[] args) {
        
		
		
		/*String fileURL = "https://repo1.maven.org/maven2/doxia/doxia-core/1.0-alpha-1/maven-metadata.xml";
        String saveDir = "/Users/hubertzhao";
        try {
            MavenLib.downloadFile(fileURL, saveDir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/
        
        //LinkedList<String> loadList = MavenLib.createDirectory("/Users/hubertzhao/Desktop/Plugins", "cd.connect.plugins", "connect-prescan-plugin", "1.1");
		try {
			LinkedList<String> loadList = MavenLib.createDirectory("/Users/hubertzhao/Desktop/Plugins", "args4j", "args4k", "blop");
			for (int i = loadList.size() - 1;i >= 0; i--) {
        		System.out.println("LIST PRINTING" + loadList.get(i));
        }
		}
		catch (DownloadFailedException e) {
			e.printStackTrace();
		}
		
    }

}
