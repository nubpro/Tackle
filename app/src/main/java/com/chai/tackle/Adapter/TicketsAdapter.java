package com.chai.tackle.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chai.tackle.Model.Ticket;
import com.chai.tackle.Model.TicketStatus;
import com.chai.tackle.Model.User;
import com.chai.tackle.Model.UserProfile;
import com.chai.tackle.Model.UserProfileCallback;
import com.chai.tackle.R;
import com.chai.tackle.TicketsFragmentDirections;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TicketsAdapter extends FirestorePagingAdapter<Ticket, TicketsAdapter.TicketHolder> {
    private Context mContext;

    public TicketsAdapter(@NonNull FirestorePagingOptions<Ticket> options, Context context) {
        super(options);

        mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull final TicketHolder holder, int i, @NonNull final Ticket ticket) {

        UserProfile.load(ticket.getCreatorId(), new UserProfileCallback() {

            @Override
            public void onCallback(User user) {
                Glide.with(mContext).load(user.getPhotoUrl()).into(holder.mCreatorAvatar);
                holder.mCreator.setText(user.getDisplayName());

                DateFormat dateFormat = new SimpleDateFormat("MMM d");
                String dateFormatted = dateFormat.format(ticket.getTimestamp());
                String ticketStatus = "[" + TicketStatus.toShortText(ticket.getStatus()) + "]";

                holder.mCategory.setText(ticket.getCategory());
                holder.mSubject.setText(ticket.getSubject());
                holder.mStatus.setText(ticketStatus);
                holder.mCreatedOn.setText(dateFormatted);
            }
        });
    }

    @NonNull
    @Override
    public TicketHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tickets_item, parent,false);
        return new TicketHolder(v);
    }

    class TicketHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mCreatorAvatar;
        TextView mCreator;
        TextView mCategory;
        TextView mSubject;
        TextView mStatus;
        TextView mCreatedOn;

        public TicketHolder(@NonNull View itemView) {
            super(itemView);

            mCreatorAvatar = itemView.findViewById(R.id.ticket_creator_avatar);
            mCreator = itemView.findViewById(R.id.ticket_creator);
            mCategory = itemView.findViewById(R.id.ticket_category);
            mSubject = itemView.findViewById(R.id.ticket_subject);
            mStatus = itemView.findViewById(R.id.ticket_status);
            mCreatedOn = itemView.findViewById(R.id.ticket_createdOn);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            // Ticket UID
            String ticketId =  getItem(position).getId();

            TicketsFragmentDirections.ActionTicketDetails action = TicketsFragmentDirections.actionTicketDetails(ticketId);
            Navigation.findNavController(v).navigate(action);
        }
    }
}