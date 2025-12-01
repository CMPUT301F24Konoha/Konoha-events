package interfaces;

import java.util.ArrayList;

import models.OnWaitingListModel;

/**
 * Simple interface for an OnWaitingListModel ArrayList callback.
 */
public interface OnWaitingListArrayListCallback {
    void onCompleted(ArrayList<OnWaitingListModel> onWaitingListModels);
}
