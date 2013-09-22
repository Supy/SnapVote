package uct.snapvote.util;

import android.content.ContentResolver;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * The ImageInputStream class provides a convenient way to pass an input stream containing an image
 * around the application.
 */
public class ImageInputStream {

    // the file input stream
    private InputStream inputStream;
    private ContentResolver contentResolver;

    // file uri
    public Uri contentUri;

    //dimension
    public int width;
    public int height;

    /**
     * Constructor. Immediately read the image dimensions from the file
     * @param resourcePath Uri string
     * @param contentResolver application content resolver
     * @throws FileNotFoundException if the Uri does not exist
     */
    public ImageInputStream(String resourcePath, ContentResolver contentResolver) throws FileNotFoundException{
        this.contentResolver = contentResolver;
        this.contentUri = Uri.parse(resourcePath);

        readImageDimensions();
    }

    /**
     * @return The File Input Stream for the image
     */
    public InputStream getInputStream(){
        try{
            // reset to beginning of stream
            reset();
            return inputStream;
        }catch(Exception e){}

        return null;
    }

    /**
     * Use the bitmap package to decode the image stream.
     * @throws FileNotFoundException
     */
    private void readImageDimensions() throws FileNotFoundException{
        reset();

        // Mark flag that we only want image dimensions on this read.
        BitmapFactory.Options preOptions = new BitmapFactory.Options();
        preOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, preOptions);

        height = preOptions.outHeight;
        width = preOptions.outWidth;
    }

    // Method to go back to the start of the stream when we want to read it again.
    private void reset() throws FileNotFoundException{
        inputStream = contentResolver.openInputStream(contentUri);
    }
}
