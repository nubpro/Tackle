package com.chai.tackle;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.chai.tackle.Model.Session;
import com.chai.tackle.Model.SessionCallable;
import com.chai.tackle.Model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.chai.tackle.Model.Constant.ROLE_NORMAL_USER;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    private static final String TAG = LoginFragment.class.getSimpleName();

    private GoogleSignInClient mGoogleSignInClient;
    static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private SignInButton signInButton;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        signInButton = getView().findViewById(R.id.google_login_button);

        // Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

        // FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Firestore instance
        db = FirebaseFirestore.getInstance();

        // Is user logged in?
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loginSuccess();
        } else {
            signInButton.setOnClickListener(loginButtonListener);
            signInButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            signInResultHandler(task);
        }
    }

    private View.OnClickListener loginButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            googleSignIn();
        }
    };

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signInResultHandler(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account); // Pass account to FirebaseAuth to get Firebase credential
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        signInButton.setVisibility(View.GONE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                String message;

                if (task.isSuccessful()) {
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    syncUserProfile(currentUser);
                } else {
                    loginFailed();
                }
            }

        });
    }

    private void loginSuccess() {
        String displayName = mAuth.getCurrentUser().getDisplayName();
        final String message = "Welcome, " + displayName + "!";

        Session.set(mAuth.getCurrentUser().getUid(), new SessionCallable() {
            @Override
            public void onCallback() {
                Snackbar.make(getActivity().findViewById(R.id.main_layout), message, Snackbar.LENGTH_SHORT).show();

                // Go to ticketsFragment
                Navigation.findNavController(getView()).navigate(R.id.ticketsFragment);

            }
        });
    }

    private void loginFailed() {
        String message = "Boo... Failed to sign in.";

        Snackbar.make(getActivity().findViewById(R.id.main_layout), message, Snackbar.LENGTH_SHORT).show();
    }

    private void syncUserProfile(final FirebaseUser user) {
        DocumentReference usersRef = db.collection("users").document(user.getUid());

        usersRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                updateUserProfile(user);
                            } else {
                                addUserProfile(user);
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    // First time logon, default user as NORMAL_USER
    private void addUserProfile(final FirebaseUser user) {
        User mUser = new User(
                user.getUid(),
                user.getEmail(),
                user.getDisplayName(),
                String.valueOf(user.getPhotoUrl()),
                ROLE_NORMAL_USER);

        db.collection("users").document(user.getUid())
                .set(mUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.w(TAG, "User profile created.");
                        loginSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "User profile failed to create.", e);
                    }
                });

    }

    private void updateUserProfile(final FirebaseUser user) {
        String displayName = user.getDisplayName();
        String photoUrl = String.valueOf(user.getPhotoUrl());

        db.collection("users").document(user.getUid())
                .update(
                        "displayName", displayName,
                        "photoUrl", photoUrl
                )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.w(TAG, "User profile updated.");
                        loginSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "User profile failed to update.", e);
                    }
                });
    }


}
