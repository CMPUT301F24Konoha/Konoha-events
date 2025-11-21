package interfaces;

import java.util.ArrayList;

import models.OnWaitingListModel;

public interface OnWaitingListArrayListCallback {
    void onCompleted(ArrayList<OnWaitingListModel> onWaitingListModels);
}
