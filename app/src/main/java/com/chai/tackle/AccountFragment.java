package com.chai.tackle;


import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chai.tackle.Model.Role;
import com.chai.tackle.Model.Session;
import com.chai.tackle.Model.User;
import com.chai.tackle.Model.UserProfile;
import com.chai.tackle.Model.UserProfileCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ImageView mAvatar;
    TextView mDisplayName;
    TextView mEmail;
    TextView mRole;

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAvatar = getView().findViewById(R.id.user_avatar);
        mDisplayName = getView().findViewById(R.id.user_display_name);
        mEmail = getView().findViewById(R.id.user_email);
        mRole = getView().findViewById(R.id.user_role);

        populateProfile();

        getView().findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                logout(v);
            }
        });
    }

    private void logout(View view) {
        mAuth.signOut();
        Navigation.findNavController(view).popBackStack(R.id.loginFragment, false);
    }

    private void populateProfile() {
        Session s = Session.getInstance();
        String roleText = Role.toText(s.getRole());

        Glide.with(getContext()).load(s.getPhotoUrl()).into(mAvatar);
        mDisplayName.setText(s.getDisplayName());
        mEmail.setText(s.getEmail());
        mRole.setText(roleText);
    }
}
