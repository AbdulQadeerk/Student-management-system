package com.example.encdecsms;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.internal.Utils;

public class ChatActivity extends AppCompatActivity {
    @BindView(R.id.relative)
    RelativeLayout mParent;
    @BindView(R.id.message)
    EditText mMessageEt;
    @BindView(R.id.recycleView)
    RecyclerView mRecycleView;
    @BindView(R.id.send)
    ImageView mSendBtn;
    @BindView(R.id.mobile)EditText mMobile;
    @BindView(R.id.code)EditText mCode;
    private MySharedPreferences sp;

    private DatabaseReference mMessageRef;
    private FirebaseRecyclerOptions options;
    private LinearLayoutManager llm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ButterKnife.bind(this);
        sp = MySharedPreferences.getInstance(this);
        mMessageRef = FirebaseDatabase.getInstance().getReference().child(Constants.COM_CHAT);

        mRecycleView.setLayoutManager(new LinearLayoutManager(this));

        options = new FirebaseRecyclerOptions.Builder<PostPojo>()
                .setQuery(mMessageRef.child(sp.getInfo("mobile")), PostPojo.class)
                .build();


        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNetworkAndSend();
            }
        });
    }

    private void checkNetworkAndSend() {
        String message = mMessageEt.getText().toString().trim();
        String mobile = mMobile.getText().toString().trim();
        String code = mCode.getText().toString().trim();
        if (message.isEmpty() || message.equals("")||mobile.isEmpty()||mobile.length()<10||code.isEmpty()) {
//            Utils.showRedSnackbar(mParent, "Please type in Message");
            Toast.makeText(getApplicationContext(),"Invalid Entries",Toast.LENGTH_SHORT).show();
        }
        else {
            sendMessage(message,mobile,code);
        }
    }

    private void sendMessage(final String message, String mobile, String code) {
        String key = mMessageRef.push().getKey();

        Map<String, String> h = new HashMap<>();
        h.put("message", message);
        h.put("time", "" + Calendar.getInstance().getTimeInMillis());
        h.put("messageBy", sp.getInfo(Constants.COM_NAME));
        Log.d("MUR", "MUR NAME send: " + sp.getInfo(Constants.COM_NAME));
        h.put("senderId", sp.getInfo(Constants.COM_USER_ID));
        h.put("messageType", "text");
        h.put("code",code);
        h.put("key", key);


        Map<String, Object> map = new HashMap<>();
        map.put(key, h);
        mMessageEt.setText("");
        mMessageRef.child(mobile).updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                Log.i("", "onComplete: " + "done");
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<PostPojo, MyViewHolder> fAdapter = new FirebaseRecyclerAdapter<PostPojo, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MyViewHolder holder, final int position, @NonNull PostPojo model) {

                final PostPojo pojo = model;
//                Log.i(TAG, "onBindViewHolderqqq: " + pojo.getMessage() + " " + pojo.getMessageBy() + " " + pojo.getTime() + " ");


                if (pojo.getSenderId() != null) {

                    holder.message.setText("This Message is Encrypted (click to decrypt");

                    String name = "";
                    if (pojo.getSenderId().equals(sp.getInfo(Constants.COM_USER_ID))) {
                        name = "Me";
                    } else {

                        name = pojo.getMessageBy();

                    }
                    holder.name.setText(name);


                    holder.mParent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDecrypDialog(pojo);
                        }
                    });










                    }


            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = null;

                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.post_layout_left, parent, false);
                    return new MyViewHolder(view);


            }



            @Override
            public void onDataChanged() {
                super.onDataChanged();

//                mRecycleView.getLayoutManager().scrollToPosition(getItemCount() - 1);

            }
        };

        mRecycleView.setAdapter(fAdapter);

        fAdapter.startListening();

    }

    private void showDecrypDialog(final PostPojo pojo) {
        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
        builder.setMessage("Enter Decryption Code)");
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.edittext_dialog,null,false);
        final EditText editText = view.findViewById(R.id.code);
        builder.setView(view);
        builder.setPositiveButton("Decrypt", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               String code = editText.getText().toString().trim();
               if (code.equals(pojo.getCode())){
                   dialog.dismiss();
                   showMessage(pojo);
               }
               else {
                   dialog.dismiss();
                   Toast.makeText(getApplicationContext(),"Invalid Code",Toast.LENGTH_SHORT).show();
               }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();


    }

    private void showMessage(PostPojo pojo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(pojo.getMessageBy());
        builder.setMessage(pojo.getMessage());
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView time, name, docName,date;
        TextView message;
        ImageView image;
        LinearLayout docParent;
        LinearLayout mParent;

        MyViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            time = (TextView) itemView.findViewById(R.id.time);
            name = (TextView) itemView.findViewById(R.id.name);
            image = (ImageView) itemView.findViewById(R.id.image);
            mParent = itemView.findViewById(R.id.parent);
            date = itemView.findViewById(R.id.date);

        }
    }


}
