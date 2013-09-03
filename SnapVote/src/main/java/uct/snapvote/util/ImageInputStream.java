package uct.snapvote.util;

import android.content.ContentResolver;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Justin on 2013/09/03.
 */
public class ImageInputStream {

    private InputStream inputStream;
    private ContentResolver contentResolver;

    public Uri contentUri;
    public int width;
    public int height;

    public ImageInputStream(String resourcePath, ContentResolver contentResolver) throws FileNotFoundException{
        this.contentResolver = contentResolver;
        this.contentUri = Uri.parse(resourcePath);

        readImageDimensions();
    }

    public InputStream getInputStream(){
        try{
            reset();
            return inputStream;
        }catch(Exception e){}

        return null;
    }

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
