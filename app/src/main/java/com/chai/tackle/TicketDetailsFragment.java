package com.chai.tackle;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chai.tackle.Adapter.ThumbnailsAdapter;
import com.chai.tackle.Model.Role;
import com.chai.tackle.Model.Session;
import com.chai.tackle.Model.Ticket;
import com.chai.tackle.Model.TicketStatus;
import com.chai.tackle.Model.User;
import com.chai.tackle.Model.UserProfile;
import com.chai.tackle.Model.UserProfileCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nguyenhoanglam.imagepicker.model.Image;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.chai.tackle.Model.Constant.ROLE_AGENT;
import static com.chai.tackle.Model.Constant.ROLE_MANAGER;
import static com.chai.tackle.Model.Constant.STATUS_CLOSED;
import static com.chai.tackle.Model.Constant.STATUS_OPEN;
import static com.chai.tackle.Model.Constant.STATUS_PENDING;
import static com.chai.tackle.Model.Constant.STATUS_SOLVED;


/**
 * A simple {@link Fragment} subclass.
 */
public class TicketDetailsFragment extends Fragment {
    private static final String TAG = "TicketDetailsFragment";
    String mTicketId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ImageView mCreatorAvatar;
    TextView mCreator;
    TextView mCreatorEmail;
    TextView mCreatorRole;
    TextView mCategory;
    TextView mSubject;
    TextView mDescription;
    TextView mStatus;
    TextView mCreatedOn;

    Button mPendingButton;
    Button mSolvedButton;
    Button mClosedButton;

    private RecyclerView mThumbnailRecyclerView;
    private ThumbnailsAdapter mThumbnailAdapter;

    private ArrayList<Image> images = new ArrayList<>();

    int mTicketStatusCode;

    public TicketDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mTicketId = getArguments().getString("ticket_id");

        return inflater.inflate(R.layout.fragment_ticket_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mCreatorAvatar = getView().findViewById(R.id.ticket_creator_avatar);
        mCreator = getView().findViewById(R.id.ticket_creator);
        mCreatorEmail = getView().findViewById(R.id.ticket_creator_email);
        mCreatorRole = getView().findViewById(R.id.ticket_creator_role);
        mCategory = getView().findViewById(R.id.ticket_category);
        mSubject = getView().findViewById(R.id.ticket_subject);
        mDescription = getView().findViewById(R.id.ticket_description);
        mStatus = getView().findViewById(R.id.ticket_status);
        mCreatedOn = getView().findViewById(R.id.ticket_createdOn);

        mPendingButton = getView().findViewById(R.id.pending_button);
        mSolvedButton = getView().findViewById(R.id.solved_button);
        mClosedButton = getView().findViewById(R.id.closed_button);

        mPendingButton.setOnClickListener(statusButtonListener);
        mSolvedButton.setOnClickListener(statusButtonListener);
        mClosedButton.setOnClickListener(statusButtonListener);

        mThumbnailRecyclerView = getView().findViewById(R.id.thumbnails_recyclerView);
        mThumbnailAdapter = new ThumbnailsAdapter(getContext(), images);
        mThumbnailRecyclerView.setAdapter(mThumbnailAdapter);
        mThumbnailRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        populateTicketDetails();

        super.onViewCreated(view, savedInstanceState);
    }

    private void populateTicketDetails() {
        db.collection("tickets").document(mTicketId)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            final Ticket ticket = document.toObject(Ticket.class);

                            final DateFormat dateFormat = new SimpleDateFormat("h:mm a, d MMMM yyyy");
                            final String dateFormatted = dateFormat.format(ticket.getTimestamp());

                            UserProfile.load(ticket.getCreatorId(), new UserProfileCallback() {
                                @Override
                                public void onCallback(User user) {
                                    if (getView() != null) {
                                        Glide.with(getView()).load(user.getPhotoUrl()).into(mCreatorAvatar);
                                    } else {
                                        Log.d("test", "Oops");
                                    }
                                    mCreator.setText(user.getDisplayName());
                                    mCreatorEmail.setText(user.getEmail());
                                    mCreatorRole.setText(Role.toText(user.getRole()));
                                    mCategory.setText(ticket.getCategory());
                                    mSubject.setText(ticket.getSubject());
                                    mDescription.setText(ticket.getDescription());
                                    mCreatedOn.setText(dateFormatted);

                                    Log.d(TAG, ticket.getAttachments().toString());

                                    ArrayList<Image> tempImages = new ArrayList<Image>();
                                    for (String path : ticket.getAttachments()) {
                                        Log.d(TAG, path);
                                        Image image = new Image(1, "", path);
                                        tempImages.add(image);
                                    }
                                    mThumbnailAdapter.setImages(tempImages);

                                    mTicketStatusCode = ticket.getStatus();
                                    updateUI();
                                }
                            });
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
    }

    private View.OnClickListener statusButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.pending_button:
                    updateTicketStatus(STATUS_PENDING);
                    break;
                case R.id.solved_button:
                    updateTicketStatus(STATUS_SOLVED);
                    break;
                case R.id.closed_button:
                    updateTicketStatus(STATUS_CLOSED);
                    break;
            }
        }
    };

    private void updateTicketStatus(final int statusCode) {
        db.collection("tickets").document(mTicketId)
                .update("status", statusCode)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mTicketStatusCode = statusCode;
                        updateUI();
                        pushNotification(statusCode);
                        Log.d(TAG, "Ticket status successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating ticket status", e);
                    }
                });
    }

    private void updateUI() {
        mStatus.setText(TicketStatus.toLongText(mTicketStatusCode));

        mPendingButton.setVisibility(View.GONE);
        mSolvedButton.setVisibility(View.GONE);
        mClosedButton.setVisibility(View.GONE);

        Session session = Session.getInstance();
        switch(session.getRole()) {
            case ROLE_AGENT:
            case ROLE_MANAGER:
                enableUpdateButtons();
                break;
        }

    }

    private void enableUpdateButtons() {
        switch(mTicketStatusCode) {
            case STATUS_OPEN:
                mPendingButton.setVisibility(View.VISIBLE);
                break;
            case STATUS_PENDING:
                mSolvedButton.setVisibility(View.VISIBLE);
                mClosedButton.setVisibility(View.VISIBLE);
                break;
            case STATUS_SOLVED:
                mClosedButton.setVisibility(View.VISIBLE);
        }
    }

    private void pushNotification(int statusCode) {
        Session session = Session.getInstance();
        String newStatusText = TicketStatus.toLongText(statusCode);

        Map<String, Object> notification = new HashMap<>();
        notification.put("ticketId", mTicketId);
        notification.put("ticket_newStatus", newStatusText);
        notification.put("ticket_updatedBy", session.getDisplayName());

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Notification added.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Notification failed to add", e);
                    }
                });

    }

}
