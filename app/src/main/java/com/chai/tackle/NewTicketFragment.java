package com.chai.tackle;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chai.tackle.Adapter.ThumbnailsAdapter;
import com.chai.tackle.Model.Ticket;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static com.chai.tackle.Model.Constant.STATUS_OPEN;

public class NewTicketFragment extends Fragment {
    private static final String TAG = NewTicketFragment.class.getSimpleName();
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    StorageReference storageRef;

    private TextView mSubject;
    private TextView mDescription;
    private AutoCompleteTextView mCategory;
    private Button mCreateTicketButton;
    private Button mAttachmentButton;

    private RecyclerView mThumbnailRecyclerView;
    private ThumbnailsAdapter mThumbnailAdapter;

    private ArrayList<Image> images = new ArrayList<>();

    private String[] TICKET_CATEGORIES = new String[]{"General Inquires", "Sales", "Billing", "Technical Issues", "Bug Reports"};

    public static NewTicketFragment newInstance() {
        return new NewTicketFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_ticket, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        mCategory = getView().findViewById(R.id.filled_exposed_dropdown);
        mCreateTicketButton = getView().findViewById(R.id.create_ticket_button);
        mSubject = getView().findViewById(R.id.newTicket_subject_editText);
        mDescription = getView().findViewById(R.id.newTicket_description_editText);
        mAttachmentButton = getView().findViewById(R.id.newTicket_attachment_button);
        mThumbnailRecyclerView = getView().findViewById(R.id.thumbnails_recyclerView);

        mThumbnailAdapter = new ThumbnailsAdapter(getContext(), images);
        mThumbnailRecyclerView.setAdapter(mThumbnailAdapter);
        mThumbnailRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        // Category
        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(
                        getContext(),
                        R.layout.dropdown_menu_popup_item,
                        TICKET_CATEGORIES);
        mCategory.setAdapter(categoryAdapter);

        mCreateTicketButton.setOnClickListener(ticketCreateListener);
        mAttachmentButton.setOnClickListener(attachmentListener);
    }

    private View.OnClickListener ticketCreateListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // IMPORTANT!
            ArrayList<String> imageUploadedList = new ArrayList<String>();

            if (images.size() > 0) {
                // Firebase storage & reference
                storage = FirebaseStorage.getInstance();

                storageRef = storage.getReference();
                for (Image image : images) {
                    uploadAttachment(image.getPath(), imageUploadedList);
                }
            } else {
                addNewTicket(imageUploadedList);
            }

        }
    };

    private View.OnClickListener attachmentListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            ImagePicker.with(NewTicketFragment.this)
                    .setToolbarColor(getHexColor(R.color.primaryColor))
                    .setStatusBarColor(getHexColor(R.color.primaryDarkColor))
                    .setToolbarTextColor(getHexColor(R.color.primaryTextColor))
                    .setToolbarIconColor(getHexColor(R.color.primaryTextColor))
                    .setProgressBarColor(getHexColor(R.color.secondaryColor))
                    .setBackgroundColor(getHexColor(R.color.backgroundColor))
                    .setCameraOnly(false)
                    .setMultipleMode(true)
                    .setFolderMode(true)
                    .setShowCamera(true)
                    .setFolderTitle("Albums")
                    .setImageTitle("Galleries")
                    .setDoneTitle("Done")
                    .setLimitMessage("You have reached selection limit")
                    .setMaxSize(5)
                    .setSavePath("Tackle")         //  Image capture folder name
                    .setSelectedImages(images)     //  Selected images
                    .setAlwaysShowDoneButton(true)
                    .setRequestCode(100)           //  Set request code, default Config.RC_PICK_IMAGES
                    .setKeepScreenOn(true)
                    .start();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            mThumbnailAdapter.setImages(images);
        }
    }

    private String getHexColor(int resourceId) {
        int color = ContextCompat.getColor(getContext(), resourceId);
        String hexColor = String.format("#%06X", (0xFFFFFF & color));

        return hexColor;
    }

    private void uploadAttachment(String imagePath, final ArrayList<String> imageUploadedList) {
        Uri file = Uri.fromFile(new File(imagePath));

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String fileExt = MimeTypeMap.getFileExtensionFromUrl(file.toString());
        String imageFileName = uid + "_" + timeStamp + "." + fileExt;

        //Log.d(TAG, imageFileName);

        final StorageReference ref = storageRef.child("user_uploads/" + imageFileName);
        final UploadTask uploadTask = ref.putFile(file);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    imageUploadedList.add(downloadUri.toString());

                } else {
                    Snackbar.make(getView(), "Attachment failed to upload. Try again.", Snackbar.LENGTH_SHORT).show();
                }

                if (storageRef.getActiveUploadTasks().size() <= 0) {
                    addNewTicket(imageUploadedList);
                }
            }
        });
    }

    private void addNewTicket(ArrayList<String> imageUploadedList) {
        String creatorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String subject = mSubject.getText().toString();
        String description = mDescription.getText().toString();
        String category = mCategory.getText().toString();
        int status = STATUS_OPEN;

        // Ticket object
        Ticket ticket = new Ticket(creatorId, category, subject, description, status, imageUploadedList);

        db.collection("tickets")
                .add(ticket)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String topic = documentReference.getId();
                        subscribeTopic(topic);

                        if (getView() != null) {
                            Snackbar.make(getView(), "Ticket has been submitted.", Snackbar.LENGTH_SHORT).show();
                            Navigation.findNavController(getView()).navigateUp();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (getView() != null) {
                            Snackbar.make(getView(), "Ticket failed to submit. Try again.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void subscribeTopic(final String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed to " + topic;
                        if (!task.isSuccessful()) {
                            msg = "Failed to subscribe to " + topic;
                        }
                        Log.d(TAG, msg);
                        //Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

