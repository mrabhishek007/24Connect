package project.android.com.connect24;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment
{
    private View mRequestFragmentView ;
    private RecyclerView mRequestList;
    private String mCurrentUser;
    private  Context context;
    private DatabaseReference mRootRef;
    private ArrayList mList = new ArrayList();
    private FriendRequestAdapter mRequestAdapter;
    private DatabaseReference mFriendDatabase;
    private ChildEventListener mChildEventListener;

    public RequestFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mRequestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);
        mRequestList =  mRequestFragmentView.findViewById(R.id.fr_accept_rv);
        mCurrentUser =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mFriendDatabase = mRootRef.child("Friend_req").child(mCurrentUser);
        // Inflate the layout for this fragment
        return  mRequestFragmentView;
    }

    private void retreiveRequest()
    {
        //Initaializing the Recycler view

        mRequestAdapter = new FriendRequestAdapter(mList);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));
        mRequestList.setHasFixedSize(true);
        mRequestList.setAdapter(mRequestAdapter);

         mChildEventListener =   mFriendDatabase.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                RequestModel mModel =  dataSnapshot.getValue(RequestModel.class);

                String req_type =  mModel.getRequest_type();

                if(req_type.equals("received"))
                {
                    String parentNode =  dataSnapshot.getKey();
                    mList.add(parentNode);
                    mRequestAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {

            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    @Override
    public void onStart()
    {
        super.onStart();
    }


    @Override
    public void onPause() {
        super.onPause();
        mFriendDatabase.removeEventListener(mChildEventListener); //Removing the childeventlistener when changing the fragment
        mList.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        retreiveRequest();
    }
}
