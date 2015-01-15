package com.ubb.imaging;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.AllTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryConstants;
import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldType;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.imaging.util.IoUtils;
import org.apache.commons.io.FileUtils;


/**
 * @author Hemed Ali
 * Date : 09-01-2015
 * Location : The University of Bergen Library
 */

public class ExifMetadataWriter {
    
    //public static final String IMAGE_DESCRIPTION = "This image is the property of the University of Bergen Library. "
                                                  //+ "More information about this image can be found at marcus.uib.no";
    
    public static final String USER_COMMENTS = "This image is the property of the University of Bergen Library. "
                                             + "More information about the image can be found by searching for the image ID at marcus.uib.no. "
                                             + "There you will also find copyright and license information for this image.";
    
    public static final String LINK_TO_RESOURCE =  "http://marcus.uib.no/instance/aggregation/";
    
    
    public void removeExifMetadata(final File jpegImageFile, final File dst)
            throws IOException, ImageReadException, ImageWriteException {
        OutputStream os = null;
        boolean canThrow = false;
        try {
            os = new FileOutputStream(dst);
            os = new BufferedOutputStream(os);

            new ExifRewriter().removeExifMetadata(jpegImageFile, os);
            canThrow = true;
        } finally {
            IoUtils.closeQuietly(canThrow, os);
        }
    }

    /**
     * This method illustrates how to add/update EXIF metadata in a JPEG file.
     * 
     * @param srcFile
     *            A source image file.
     * @param imageId
     * @param dstFile
     *            The output file.
     * @throws IOException
     * @throws ImageReadException
     * @throws ImageWriteException
     */
    public void changeExifMetadata(File srcFile, File dstFile , String imageId)
            throws IOException, ImageReadException, ImageWriteException {
        OutputStream os = null;
        boolean canThrow = false;
        try {
            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            final ImageMetadata metadata = (ImageMetadata)Imaging.getMetadata(srcFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata)metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no Exif metadata is found.
                final TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif) {
                    // TiffImageMetadata class is immutable (read-only).
                    // TiffOutputSet class represents the Exif data to write.
                    //
                    // Usually, we want to update existing Exif metadata by
                    // changing
                    // the values of a few fields, or adding a field.
                    // In these cases, it is easiest to use getOutputSet() to
                    // start with a "copy" of the fields read from the image.
                    outputSet = exif.getOutputSet();
                }
            }

            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other
            // existing tags.
            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }

            {
                // Example of how to add a field/tag to the output set.
                //
                // Note that you should first remove the field/tag if it already
                // exists in this directory, or you may end up with duplicate
                // tags. See above.
                //
                // Certain fields/tags are expected in certain Exif directories;
                // Others can occur in more than one directory (and often have a
                // different meaning in different directories).
                //
                // TagInfo constants often contain a description of what
                // directories are associated with a given tag.
                //
                // see
                // org.apache.commons.imaging.formats.tiff.constants.AllTagConstants
                //
                 
                 final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
                
                
                 updateImageUniqueId(exifDirectory, imageId);
                 updateDocumentName(exifDirectory , imageId);
                 updateUserComments(exifDirectory);
                 updateImageDescription(exifDirectory, LINK_TO_RESOURCE.concat(imageId));
               
                
                  // AllTagConstants.TIFF_TAG_IMAGE_DESCRIPTION
                  // AllTagConstants.TIFF_TAG_DOCUMENT_NAME
                  // AllTagConstants.TIFF_TAG_COPYRIGHT   
                
             }

              os = new BufferedOutputStream(new FileOutputStream(dstFile));
               
              
               new ExifRewriter().updateExifMetadataLossless(srcFile, os,
                    outputSet);

              canThrow = true;
        } finally {
            IoUtils.closeQuietly(canThrow, os);
                    
                 //delete the source file
                 srcFile.delete();
                //move the new file with new metadata to the source file path (it's like copy and delete).
                FileUtils.moveFile(dstFile, srcFile); 
            
        }
    }
    
   
         private void updateImageDescription (TiffOutputDirectory exifDirectory , String imageDescription)
            throws IOException, ImageReadException, ImageWriteException 
            {
                
                 // Note that you should first remove the field/tag if it already
                // exists in this directory, or you may end up with duplicate tag.
                // This method won't fail if the field does not exist.
                 exifDirectory
                         .removeField(AllTagConstants.TIFF_TAG_IMAGE_DESCRIPTION);
                
                 exifDirectory
                         .add(AllTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, imageDescription);     
            }
         
               
         private void updateDocumentName(TiffOutputDirectory exifDirectory, String documentName)
            throws IOException, ImageReadException, ImageWriteException 
          {
         
                 exifDirectory
                         .removeField(AllTagConstants.TIFF_TAG_DOCUMENT_NAME);
                 exifDirectory
                         .add(AllTagConstants.TIFF_TAG_DOCUMENT_NAME, documentName);

            }
         
        private void updateUserComments (TiffOutputDirectory exifDirectory)
            throws IOException, ImageReadException, ImageWriteException 
         {
                 exifDirectory
                         .removeField(AllTagConstants.EXIF_TAG_USER_COMMENT);
                
                 exifDirectory
                         .add(AllTagConstants.EXIF_TAG_USER_COMMENT, USER_COMMENTS);
         }
        
        
            private void updateImageUniqueId (TiffOutputDirectory exifDirectory, String newId)
                throws IOException, ImageReadException, ImageWriteException 
             {
                     exifDirectory
                             .removeField(TiffConstants.EXIF_TAG_IMAGE_UNIQUE_ID);
                     
                       TiffOutputField idField = new TiffOutputField
                                 (TiffConstants.EXIF_TAG_IMAGE_UNIQUE_ID,
                                         FieldType.ASCII, 
                                         newId.getBytes("UTF-8").length, 
                                         newId.getBytes("UTF-8"));
                     
                     
                     exifDirectory.add(idField);
             }
            
            
            public static TiffOutputField getNewXPTitle(TiffOutputSet outputSet, String newTitle) 
            {
                TiffOutputField tiffOutputField = null;
               try {
                    tiffOutputField = new TiffOutputField(
                            TiffConstants.EXIF_TAG_XPTITLE,
                            FieldType.BYTE,
                            newTitle.getBytes("UTF-16").length, newTitle.getBytes("UTF-16"));


                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(ExifMetadataWriter.class.getName()).log(Level.SEVERE, ex.getMessage());
                }
                return tiffOutputField;
            }
         

       
        public void readMetadata(File file) throws ImageReadException 
        {
            ImageMetadata metadata = null;
        
          try {
                 metadata = (ImageMetadata)Imaging.getMetadata(file);
                 
                 if (metadata instanceof JpegImageMetadata) 
                 {
                    JpegImageMetadata jpegMetadata = (JpegImageMetadata)metadata;

                    System.out.println("=============================="
                            + "\nReading From the File: " + file.getPath());
                     System.out.println("==============================");

                     System.out.print(jpegMetadata.getExif());
                 }
        
           } catch (ImageReadException | IOException e) {
             e.printStackTrace();
             }     
         
          }
        
     
     public void modifyXpTitle(File fileIn, File fileOut, String newValue) throws Exception 
     {
        TiffImageMetadata exif;
         ImageMetadata meta = Imaging.getMetadata(fileIn);
        if (meta instanceof JpegImageMetadata) 
          {
               exif = ((JpegImageMetadata)meta).getExif();
          } 
          else if (meta instanceof TiffImageMetadata) {
            exif = (TiffImageMetadata)meta;
         } else {
            return;
        }
         TiffOutputSet outputSet = exif.getOutputSet();
         TiffOutputDirectory exifDir = outputSet.findDirectory(TiffDirectoryConstants.DIRECTORY_TYPE_EXIF);
         exifDir.removeField(AllTagConstants.EXIF_TAG_XPTITLE);
         exifDir.add(AllTagConstants.EXIF_TAG_XPTITLE, newValue);

         ExifRewriter rewriter = new ExifRewriter();
         FileOutputStream fos = null;
         try {
            fos = new FileOutputStream(fileOut);
            rewriter.updateExifMetadataLossy(fileIn, fos, outputSet);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
    

    /**
     * This method illustrates how to set the GPS values in JPEG EXIF metadata.
     * 
     * @param jpegImageFile
     *            A source image file.
     * @param dst
     *            The output file.
     * @throws IOException
     * @throws ImageReadException
     * @throws ImageWriteException
     */
    public void setExifGPSTag(final File jpegImageFile, final File dst) throws IOException,
            ImageReadException, ImageWriteException {
        OutputStream os = null;
        boolean canThrow = false;
        try {
            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            final ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no Exif metadata is found.
                final TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif) {
                    // TiffImageMetadata class is immutable (read-only).
                    // TiffOutputSet class represents the Exif data to write.
                    //
                    // Usually, we want to update existing Exif metadata by
                    // changing
                    // the values of a few fields, or adding a field.
                    // In these cases, it is easiest to use getOutputSet() to
                    // start with a "copy" of the fields read from the image.
                    outputSet = exif.getOutputSet();
                }
            }

            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other
            // existing tags.
            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }

            {
                // Example of how to add/update GPS info to output set.

                // New York City
                final double longitude = -74.0; // 74 degrees W (in Degrees East)
                final double latitude = 40 + 43 / 60.0; // 40 degrees N (in Degrees
                // North)

                outputSet.setGPSInDegrees(longitude, latitude);
            }

            os = new FileOutputStream(dst);
            os = new BufferedOutputStream(os);

            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
                    outputSet);
            canThrow = true;
        } finally {
            IoUtils.closeQuietly(canThrow, os);
        }
    }
    

}
