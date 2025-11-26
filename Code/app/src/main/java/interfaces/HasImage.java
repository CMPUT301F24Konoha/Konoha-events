package interfaces;

import android.graphics.Bitmap;

public interface HasImage {
    String getId();
    Bitmap getImageBitmap();
    String getUploaderId();
    String getImageContext();
}
