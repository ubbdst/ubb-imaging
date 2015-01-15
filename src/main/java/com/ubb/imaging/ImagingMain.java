package com.ubb.imaging;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Hemed Ali
 * Date : 09-01-2015
 * Location : The University of Bergen Library.
 * Abstract : This application writes exif metadata to the JPEG images 
 *            (currently only Image ID, Document Name, Image Description and User Comments).
 *            The system reads the XML file as the first argument from the command line 
 *            so as to know which metadata to write and where are the location of the files.
 *            The XML file must adhere to our structure. 
 *            See ReadMe.txt for an example of our XML structure.
 */
  public class ImagingMain 
  {     
    
        public static final String TEMP_FILE_NAME = "temp.jpg";
        public static final String TEMP_FOLDER_NAME = "exifMetadataTemp";
    
        public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, 
               TransformerException, ImageReadException, ImageWriteException
          {          
                  
                  String systemTempDirectoryPath =  FilenameUtils.concat(FileUtils.getTempDirectoryPath(), TEMP_FOLDER_NAME);
                  long startTime = System.currentTimeMillis();
                  
                  //get the system argument "filePath" from the command line
                  String pathToXml =  System.getProperty("filePath"); /**args[0];**/   
                  
                  
                  //If no argument passed, notify a user.
                  if(pathToXml == null || pathToXml.trim().isEmpty())
                   {
                      Logger.getLogger(ImagingMain.class.getName()).log(Level.SEVERE , "Please specify the path to your XML file as an argument. Example: java -jar -DfilePath=\"path to xml file\" MyApp.jar");  
                   } 
                  else //if everything is Ok, continue with the logic.
                  {
                      ExifMetadataWriter exif = new ExifMetadataWriter();

                      //create new temp folder
                      File tempFolderPath = new File(systemTempDirectoryPath);
                      tempFolderPath.mkdir();

                      //create a temporary file within the temp folder
                      String tempFilePath = FilenameUtils.concat(systemTempDirectoryPath , TEMP_FILE_NAME);
                      File  tempFile = new File(tempFilePath);

                      //Build a document factory for accesing XML file
                      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                              .newInstance();
                      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

                      System.out.println("===============================================\n"
                              + "Starting UBB Imaging Application... "
                              + "\nReading From File: " + pathToXml
                              + "\n===============================================");
                      
                      Document document = docBuilder.parse(pathToXml);
                      Logger.getLogger(ImagingMain.class.getName()).log(Level.INFO , "Extracting Metadata From: {0}", pathToXml);

                      processDocument(document.getDocumentElement(), exif, tempFile);
                      
                      System.out.println("=============================================\n"
                              + "Finished Processing Images. "
                              + "\nTime taken: " +  TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - startTime)
                              + " minutes"
                              + "\n=============================================");
                     }
                 
             }  
      
                   /* 
                    * Method for processing XML document
                    * @param node
                   **/
                   public static void processDocument(Node node, ExifMetadataWriter exif , File tempFile) throws 
                     IOException, ImageReadException , ImageWriteException
                   {
                        //We are only interested with element node of type "file", see xml document. 
                         if(node.getNodeName().equalsIgnoreCase("file"))
                         {
                            Node signatureNode = node.getAttributes().getNamedItem("n");
                            String signature = signatureNode.getNodeValue();

                            Node pathNode = node.getAttributes().getNamedItem("path");
                            String path = pathNode.getNodeValue();

                            String fileNameWithoutPath = FilenameUtils.getName(path);
                            File srcFile = new File(path);
                            
                            //check if the file exists
                            if(srcFile.exists())
                            {
                                //check if the file are of type jpeg. 
                                //At the moment, We are processing only Jpeg images of versions _sm, _lg, _xl, _md , _th (small, medium, thumbnail, large, extra-large keywords)
                                if(fileNameWithoutPath.toLowerCase().endsWith(".jpg"))
                                 {
                                    Logger.getLogger(ImagingMain.class.getName()).log(Level.INFO , "Writing metadata to: {0}", path);
                                    exif.changeExifMetadata(srcFile, tempFile, signature);
                                    //exif.readMetadata(new File(path));
                                 }
                                  else
                                 {
                                   Logger.getLogger(ImagingMain.class.getName()).log(Level.WARNING , "{0} is not JPEG. No action was taken.", path);
                                 }
                            }
                            else
                            {
                               Logger.getLogger(ImagingMain.class.getName()).log(Level.WARNING , "Path {0} does not exist. No action was taken.", path);
                            }
                          }

                           //Recursive call to the children of the node.
                           NodeList nodeList = node.getChildNodes();
                           for (int i = 0; i < nodeList.getLength(); i++) 
                          {
                            Node currentNode = nodeList.item(i);
                            if (currentNode.getNodeType() == Node.ELEMENT_NODE) 
                            { 
                                //calls this method for all the children which is Element
                                 processDocument(currentNode, exif, tempFile);
                            }
                         }
                   }
  
}





