package models;

import androidx.annotation.Nullable;

import constants.DatabaseConstants;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@Getter
@AllArgsConstructor
@Builder
public class UserModel {
    @NonNull
    private String id;
    @NonNull
    private DatabaseConstants.USER_TYPE userType;
    @NonNull
    private String username;
    @NonNull
    private String password;
    @Nullable
    private String deviceId;
    @Nullable
    private String fullName;
    @Nullable
    private String phoneNumber;
}
