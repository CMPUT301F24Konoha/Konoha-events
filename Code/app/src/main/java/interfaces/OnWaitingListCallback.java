package interfaces;

import java.util.ArrayList;

import models.OnWaitingListModel;

/**
 * Simple interface for an OnWaitingListModel callback.
 */
public interface OnWaitingListCallback {
    void onCompleted(OnWaitingListModel onWaitingListModel);
}
