package com.chai.tackle.Model;

import com.google.firebase.auth.FirebaseUser;

public class Session {
    private static Session instance;

    private static String mUid;
    private static String mEmail;
    private static String mDisplayName;
    private static String mPhotoUrl;
    private static int mRole;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public static void set(String uuid, final SessionCallable callback) {
        UserProfile.load(uuid, new UserProfileCallback() {
            @Override
            public void onCallback(User user) {
                mUid = user.getUid();
                mEmail = user.getEmail();
                mDisplayName = user.getDisplayName();
                mRole = user.getRole();
                mPhotoUrl = user.getPhotoUrl();

                callback.onCallback();
            }
        });
    }

    public static String getUid() {
        return mUid;
    }

    public static String getEmail() {
        return mEmail;
    }

    public static String getDisplayName() {
        return mDisplayName;
    }

    public static String getPhotoUrl() {
        return mPhotoUrl;
    }

    public static int getRole() {
        return mRole;
    }
}
