# ubb-imaging

 This application writes exif metadata to the JPEG images (currently only <code> Image ID, Document Name, Image Description and User Comments </code>). 
 The system reads the XML file as the first argument from the command line so as to know which metadata to write and where are the location of the files.
 The XML file must adhere to our structure. Please see the example of the XML structure in <code>/data/test.xml</code>
 

### How to run the application

- Download and extract the zip file from <code> /targe/releases/ubb-imaging-1.0-SNAPSHOT.zip</code>. The file contains all the dependencies of the project. Hence when extracting, you must keep all the files in the same folder.

- Locate your XML file (which you want the application to read from), as illustrated above.

- Go to command line and type : <code> java -jar -DfilePath="path to your xml file"  path to ubb-imaging-1.0-SNAPSHOT.jar</code>. For example: <code> java -jar -DfilePath="/data/kole.xml" ubb-imaging-1.0-SNAPSHOT.jar</code>
The application will look for the system variable <code>filePath</code> and process the file accordingly. The system provides useful error handlings and logs on the foreground so that you can see the output messages as it process the images.
- You are done.


