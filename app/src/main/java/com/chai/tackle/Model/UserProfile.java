package com.chai.tackle.Model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfile {
    private final static String TAG = UserProfile.class.getSimpleName();

    public static void load(String uid, final UserProfileCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);

                                callback.onCallback(user);
                            } else {
                                Log.d(TAG, "User does not exists.", task.getException());
                            }
                        } else {
                            Log.d(TAG, "User profile failed to load.", task.getException());
                        }
                    }
                });

    }
}
