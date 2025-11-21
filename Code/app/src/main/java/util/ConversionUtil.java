package util;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ConversionUtil {
    public static String convertUriToBase64(Uri uri, ContentResolver contentResolver) throws IOException {
        String base64String;

        Bitmap bitmap;
        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        return base64String;
    }
}
