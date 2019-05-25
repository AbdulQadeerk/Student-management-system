package com.example.encdecsms;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    @BindView(R.id.relative)
    RelativeLayout mParent;
    @BindView(R.id.email)
    EditText mEmailEt;
    @BindView(R.id.login)
    Button mLoginBtn;
    @BindView(R.id.password)
    EditText mPasswordEt;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private MySharedPreferences sp;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        ButterKnife.bind(this, view);
        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.COM_USERS);
        sp = MySharedPreferences.getInstance(getContext());
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDataAndRegister();
            }
        });



        return view;
    }

    private void checkDataAndRegister() {
        String email, password;
        email = mEmailEt.getText().toString().trim();


        password = mPasswordEt.getText().toString().trim();
        if (email.isEmpty() || email.equals("")) {

            return;
        }
        if (password.length() < 6) {

        } else {
            checkNetworkAndLogin(email, password);
        }

    }

    private void checkNetworkAndLogin(final String email, final String password) {


            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        getUserData(email, password);
                    } else {
                    }
                }
            });

    }

    private String step;

    private void getUserData(final String email, final String password) {

        mUserRef.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child(Constants.COM_NAME).getValue(String.class);
                String mobile = dataSnapshot.child("mobile").getValue(String.class);
                Log.d("MUR", "MUR name login: " + name);
                Toast.makeText(getContext(),"Sign In Successful",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(),ChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                sp.setInfo(Constants.COM_NAME, name);
                sp.setInfo(Constants.COM_EMAIL, email);
                sp.setInfo(Constants.COM_PASSWORD, password);
                sp.setInfo(Constants.COM_USER_ID, mAuth.getUid());
                sp.setInfo("mobile",mobile);

                sp.setLogin("1");

                FirebaseMessaging.getInstance().subscribeToTopic(mobile);




            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }


}
