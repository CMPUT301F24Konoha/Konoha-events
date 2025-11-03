package interfaces;

import java.util.ArrayList;

import models.UserModel;

public interface UserModelArrayListCallback {
    void onCompleted(ArrayList<UserModel> userModels);
}
