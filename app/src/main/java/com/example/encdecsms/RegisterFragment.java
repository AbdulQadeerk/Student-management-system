package com.example.encdecsms;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {
    @BindView(R.id.relative)
    RelativeLayout  mParent;
    @BindView(R.id.name)
    EditText mNameEt;
    @BindView(R.id.email)
    EditText mEmailEt;
    @BindView(R.id.login)
    Button mLoginBtn;
    @BindView(R.id.password)
    EditText mPasswordEt;
    @BindView(R.id.mobile)EditText mMobileEt;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private MySharedPreferences sp;
    private List<String> mTermsList = new ArrayList<>();


    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);
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

        final ArrayAdapter<String> termsAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,mTermsList);


        return view;
    }

    private void checkDataAndRegister() {
        String email, name, password;
        email = mEmailEt.getText().toString().trim();
        name = mNameEt.getText().toString().trim();
        String mobile = mMobileEt.getText().toString();

        if (name.isEmpty() || name.equals("")) {
            return;
        }
        password = mPasswordEt.getText().toString().trim();
        if (email.isEmpty() || email.equals("")) {

            return;
        }
        if (password.length() < 6) {
            return;
        }

        else {
            checkNetworkAndLogin(name, email, password,mobile);
        }

    }

    private void checkNetworkAndLogin(final String name, final String email, final String password, final String mobile) {


        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    saveData(name, email, password,mobile);
                } else {
                }
            }
        });
    }

    private void saveData(final String name, final String email, final String password, final String mobile) {
        final Map<String, String> map = new HashMap<>();
        map.put(Constants.COM_NAME, name);
        map.put(Constants.COM_PASSWORD, password);
        map.put(Constants.COM_EMAIL, email);
        map.put(Constants.COM_STEP, "1");
        map.put("mobile",mobile);
        map.put(Constants.COM_USER_ID, mAuth.getUid());

        mUserRef.child(mAuth.getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(),"Sign Up Successful",Toast.LENGTH_LONG).show();
                    FirebaseMessaging.getInstance().subscribeToTopic(mobile);
                    Intent intent = new Intent(getActivity(),ChatActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    sp.setInfo(Constants.COM_NAME, name);
                    sp.setInfo(Constants.COM_EMAIL, email);

                    sp.setInfo(Constants.COM_USER_ID, mAuth.getUid());
                    sp.setInfo(Constants.COM_PASSWORD, password);
                    sp.setInfo("mobile",mobile);
                    sp.setLogin("1");


                } else {
                }
            }
        });
    }

}
