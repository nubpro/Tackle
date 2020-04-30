package com.chai.tackle;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavHost;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private BottomNavigationView mBottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        mToolbar = findViewById(R.id.toolbar);
        mBottomNavigation = findViewById(R.id.bottom_navigation);

        // Top level destinations
        int[] top_level_dest = {R.id.loginFragment, R.id.accountFragment, R.id.ticketsFragment};

        // Setup navigation controller
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(top_level_dest).build();
        NavigationUI.setupWithNavController(mToolbar, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(mBottomNavigation, navController);

        navController.addOnDestinationChangedListener(navDestinationChangeListener);
    }

    private NavController.OnDestinationChangedListener navDestinationChangeListener = new NavController.OnDestinationChangedListener() {
        @Override
        public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
            switch(destination.getId()) {
                case R.id.loginFragment:
                case R.id.newTicketFragment:
                case R.id.ticketDetailsFragment:
                    mBottomNavigation.setVisibility(View.GONE);
                    break;
                default:
                    mBottomNavigation.setVisibility(View.VISIBLE);
                    break;
            }

            switch (destination.getId()) {
                case R.id.loginFragment:
                    mToolbar.setVisibility(View.GONE);
                    break;
                default:
                    mToolbar.setVisibility(View.VISIBLE);
            }
        }
    };
}
