package com.example.user.awsex.GoogleSignIn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.user.awsex.DynamoDb.dynamo_db;
import com.example.user.awsex.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.Status;

public class GSignIn extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    Button b1;
    ImageView propic;
    TextView user,em;
    LinearLayout ll;
    SignInButton SignIn;
    GoogleApiClient googleApiClient;
    private static final int Req_code=9001;
    private static final int Sign_out=8788;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsign_in);

        ll=(LinearLayout)findViewById(R.id.ProfSec);
        propic=(ImageView)findViewById(R.id.imageView);
        user=(TextView) findViewById(R.id.username);
        em=(TextView)findViewById(R.id.email);
        SignIn=(SignInButton)findViewById(R.id.GsignIn);
        b1=(Button)findViewById(R.id.signout);

         SignIn.setOnClickListener(this);
         b1.setOnClickListener(this);

         ll.setVisibility(View.VISIBLE);
        GoogleSignInOptions signInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

        googleApiClient=new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions).build();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View view) {
          switch (view.getId()){
              case R.id.GsignIn: signin();break;

              case R.id.signout: signout();break;

          }
    }

    private void signin(){
          Intent intent= Auth.GoogleSignInApi.getSignInIntent(googleApiClient);

          startActivityForResult(intent,Req_code);
    }
    private void signout(){

        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResolvingResultCallbacks<Status>(this,Sign_out) {
            @Override
            public void onSuccess(@NonNull Status status) {
                updateUI(false);
            }

            @Override
            public void onUnresolvableFailure(@NonNull Status status) {

            }
        });

    }
    private void handleresult(GoogleSignInResult result){
       if(result.isSuccess()){
           GoogleSignInAccount account=result.getSignInAccount();
           String name=account.getDisplayName();
           String email=account.getEmail();
           String  imgurl=account.getPhotoUrl().toString();
           em.setText(email);
           user.setText(name);
           Glide.with(this).load(imgurl).into(propic);
           updateUI(true);
           SharedPreferences sharedPreferences=getSharedPreferences("User", MODE_PRIVATE);
           SharedPreferences.Editor edit=sharedPreferences.edit();
           edit.putString("umail",account.getEmail());
           edit.putString("dispname",account.getDisplayName());
           edit.putString("imgurl",account.getPhotoUrl().toString());
           edit.putString("name",account.getGivenName());

           edit.commit();

           startActivity(new Intent(GSignIn.this, dynamo_db.class));

       }
       else{
           updateUI(false);
       }
    }
    private void updateUI(Boolean islogin){
       if(islogin){
           ll.setVisibility(View.VISIBLE);
           SignIn.setVisibility(View.VISIBLE);
       }
       else{
           ll.setVisibility(View.VISIBLE);
           SignIn.setVisibility(View.VISIBLE);
       }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Req_code){
            GoogleSignInResult  result=Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleresult(result);
        }
    }
}
