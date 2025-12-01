package interfaces;

import constants.DatabaseConstants;

/**
 * Simple interface for a UserType callback.
 */
public interface UserTypeCallback {
    void onCompleted(DatabaseConstants.USER_TYPE userType);
}
