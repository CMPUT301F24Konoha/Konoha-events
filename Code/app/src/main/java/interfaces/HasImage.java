package interfaces;

import android.graphics.Bitmap;

/**
 * Interface to be attached to Model which have images. This interface is used in the admin
 * image view for displaying various images stored throughout the system.
 */
public interface HasImage {
    String getId();
    Bitmap getImageBitmap();
}
