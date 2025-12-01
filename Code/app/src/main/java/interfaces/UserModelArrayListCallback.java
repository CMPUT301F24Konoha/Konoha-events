package interfaces;

import java.util.ArrayList;

import models.UserModel;

/**
 * Simple interface for a UserType ArrayList callback.
 */
public interface UserModelArrayListCallback {
    void onCompleted(ArrayList<UserModel> userModels);
}
