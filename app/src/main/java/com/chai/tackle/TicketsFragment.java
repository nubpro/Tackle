package com.chai.tackle;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import com.chai.tackle.Adapter.TicketsAdapter;
import com.chai.tackle.Model.Session;
import com.chai.tackle.Model.Ticket;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import static com.chai.tackle.Model.Constant.ROLE_AGENT;
import static com.chai.tackle.Model.Constant.ROLE_MANAGER;
import static com.chai.tackle.Model.Constant.ROLE_NORMAL_USER;


/**
 * A simple {@link Fragment} subclass.
 */
public class TicketsFragment extends Fragment {
    private FloatingActionButton mNewTicketFAB;
    private RecyclerView ticketsRecyclerView;
    private TicketsAdapter ticketsAdapter;

    public TicketsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tickets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mNewTicketFAB = getView().findViewById(R.id.new_ticket_fab);
        ticketsRecyclerView = getView().findViewById(R.id.tickets_recyclerView);

        mNewTicketFAB.setOnClickListener(newTicketListener);

        Session session = Session.getInstance();

        Query query = FirebaseFirestore.getInstance()
                .collection("tickets");

        switch(session.getRole()) {
            case ROLE_AGENT:
            case ROLE_MANAGER:
                query = query.orderBy("timestamp", Query.Direction.DESCENDING);
                mNewTicketFAB.setVisibility(View.GONE);
                break;

            case ROLE_NORMAL_USER:
            default:
                mNewTicketFAB.setVisibility(View.VISIBLE);
                query = query
                        .whereEqualTo("creatorId", session.getUid())
                        .orderBy("timestamp", Query.Direction.DESCENDING);
        }

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<Ticket> options = new FirestorePagingOptions.Builder<Ticket>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Ticket.class)
                .build();

        ticketsAdapter = new TicketsAdapter(options, getContext());
        ticketsRecyclerView.setAdapter(ticketsAdapter);
        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        super.onViewCreated(view, savedInstanceState);
    }

    private View.OnClickListener newTicketListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Navigation.findNavController(v).navigate(R.id.newTicketFragment);
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        ticketsAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        ticketsAdapter.stopListening();
    }
}
