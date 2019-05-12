package com.project.aifoto;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotificationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private DocumentSnapshot lastVisible;

    private RecyclerView recyclerViewNotificationList;
    private List<Notification> notificationList;
    private List<User> notificationSenderList;
    private NotificationRecyclerAdapter notificationRecyclerAdapter;

    private OnFragmentInteractionListener mListener;

    private FirebaseFirestore firebaseFirestore;

    private String currentUserId;

    private ViewGroup container;

    public NotificationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotificationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.container = container;


        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerViewNotificationList = view.findViewById(R.id.recyclerViewNotificationList);

        notificationList = new ArrayList<>();
        notificationSenderList = new ArrayList<>();
        notificationRecyclerAdapter = new NotificationRecyclerAdapter(container.getContext(),notificationList, notificationSenderList);

        recyclerViewNotificationList.setHasFixedSize(true);
        recyclerViewNotificationList.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerViewNotificationList.setAdapter(notificationRecyclerAdapter);

        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadData();

        return view;
    }

    private void loadData() {
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {

            notificationSenderList.clear();
            notificationList.clear();

            final String path = "Users/" + currentUserId + "/Notifications";

            recyclerViewNotificationList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if(reachedBottom){

                        String desc = lastVisible.getString("desc");
                        Toast.makeText(container.getContext(),"Reached : "+desc, Toast.LENGTH_LONG).show();
                        loadMoreNotification(path);
                    }

                }
            });

            Query firstQuery = firebaseFirestore.collection(path).orderBy("timestamp",Query.Direction.ASCENDING).limit(10);

            loadNotification(firstQuery);

//            firebaseFirestore.collection(path).addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
//                @Override
//                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//
//                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
//                        notificationList.clear();
//
//                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
//
//                            final Notification notifications = doc.getDocument().toObject(Notification.class);
//                            String comment_owner_id = notifications.getComment_owner_id();
//
//                            //if(comment_owner_id.equals(currentUserId)) {
//                                //String userInfoPath = "Users/"+currentUserId;
//                                firebaseFirestore.collection("Users").document(comment_owner_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                        if(task.isSuccessful()){
//                                            User notificationSender = task.getResult().toObject(User.class);
//                                            notificationSenderList.add(notificationSender);
//                                            notificationList.add(notifications);
//                                            notificationRecyclerAdapter.notifyDataSetChanged();
//                                        }
//                                    }
//                                });
//
//                            //}
//                        }
//                    }
//                }
//            });
        }
    }

    private void loadNotification(Query nextQuery) {
        nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
        //           notificationList.clear();
                    lastVisible = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.size() - 1);
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        final Notification notifications = doc.getDocument().toObject(Notification.class);
                        String comment_owner_id = notifications.getComment_owner_id();

                        //if(comment_owner_id.equals(currentUserId)) {
                        //String userInfoPath = "Users/"+currentUserId;
                        firebaseFirestore.collection("Users").document(comment_owner_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    User notificationSender = task.getResult().toObject(User.class);
                                    notificationSenderList.add(0,notificationSender);
                                    notificationList.add(0,notifications);
                                    notificationRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        });

                        //}
                    }
                }
            }
        });
    }

    private void loadMoreNotification(String path) {
        Query nextQuery = firebaseFirestore.collection(path)
                .orderBy("timestamp",Query.Direction.ASCENDING)
                .startAfter(lastVisible)
                .limit(10);
        loadNotification(nextQuery);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
