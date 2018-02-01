package com.example.user.awsex;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.example.user.awsex.DynamoDb.dynamo_db;
import com.example.user.awsex.Polly.awspolly;
import com.example.user.awsex.Rekognition.Img_Compare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {

    Uri imageUri,selectedImageUri;
    private static final int PICK_Camera_IMAGE=2;
    private static final int PICK_IMAGE=1;
    String Path;
    File IMG;
    ArrayList<String> key_list;
    ListView listView;
    ArrayAdapter<String> adapter;
    String itemVal;
    ImageView img;
    CognitoCachingCredentialsProvider credentialsProvider;
    CognitoSyncManager syncManager;
    Button b1,b2,b3,b4,list,polly;
    AmazonS3 s3;
    EditText keyedit;
    TransferUtility transferUtility;
//    AmazonSQSClient sqsClient;
    AmazonSQSAsyncClient sqsClient;
    AmazonSNSClient snsClient;
    GetQueueUrlResult result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        img=(ImageView)findViewById(R.id.image);
        b1=(Button)findViewById(R.id.button4);
        b2=(Button)findViewById(R.id.download);
        b3=(Button)findViewById(R.id.compare);
        b4=(Button)findViewById(R.id.db);
        list=(Button)findViewById(R.id.list);
        polly=(Button)findViewById(R.id.Polly);
        keyedit=(EditText)findViewById(R.id.editText3);
        listView=(ListView)findViewById(R.id.keylist);
        credentialsProvider=new CognitoCachingCredentialsProvider(getApplicationContext(),
                "us-west-2:20e04e1d-cd9d-46ca-9305-93fe4f13f312",
                Regions.US_WEST_2);


        s3=new AmazonS3Client(credentialsProvider);


//        sqsClient = new AmazonSQSClient(credentialsProvider);
        sqsClient = new AmazonSQSAsyncClient(credentialsProvider);
        sqsClient.setRegion(Region.getRegion(Regions.US_WEST_2));

        snsClient =new AmazonSNSClient(credentialsProvider);
        snsClient.setRegion(Region.getRegion(Regions.US_WEST_2));



        sqsClient.createQueueAsync(new CreateQueueRequest().withQueueName("Boww"));
        new URL_Task().execute();





        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 99);
            }
        });
        polly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SQS_Task().execute();
                //startActivity(new Intent(Main2Activity.this,awspolly.class));

            }
        });
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageUri==null){
                    Toast.makeText(Main2Activity.this, "Select a pic", Toast.LENGTH_SHORT).show();
                }
                else if(keyedit.getText().toString().isEmpty()){
                    Toast.makeText(Main2Activity.this, "Enter the key to upload ", Toast.LENGTH_SHORT).show();
                }
                else{
                    new MyTask_S3_IMG_Upload().execute();
                }

            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(keyedit.getText().toString().isEmpty()){
                    Toast.makeText(Main2Activity.this, "Enter the key to download", Toast.LENGTH_SHORT).show();
                }
                new MyTask_S3_IMG_Down().execute();
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Main2Activity.this, Img_Compare.class));
            }
        });
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Main2Activity.this, dynamo_db.class));
            }
        });
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MyTask_Listing().execute();


            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 99 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            imageUri=uri;
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Log.d(TAG, String.valueOf(bitmap));
            img.setImageBitmap(bitmap);
            Path = ImageFilePath.getPath(getApplicationContext(),uri);
            System.out.println("PATH IS "+Path);
            IMG = new File(Path);

        }
    }
    public class URL_Task extends  AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... strings) {
            GetQueueUrlRequest request =new GetQueueUrlRequest().withQueueName("Boww");
            result=sqsClient.getQueueUrl(request);
            System.out.println("Url "+result.getQueueUrl());
            return null;
        }
    }
    public class SQS_Task extends  AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... strings) {

            /

            SendMessageRequest sendMessageRequest=new SendMessageRequest().withMessageBody("Bowwwww").withQueueUrl(result.getQueueUrl());
             SendMessageResult msresult=sqsClient.sendMessage(sendMessageRequest);
            System.out.println("Bowwwwwww "+msresult.getMessageId());

//            snsClient.createTopic("Kiruba");
//            PublishRequest prequest=new PublishRequest().withTopicArn("arn:aws:sns:us-west-2:503719577572:Kiruba").withMessage("Bow Bow");
//            PublishResult presult=snsClient.publish(prequest);
//            System.out.println("published : "+presult.getMessageId());


            return null;
        }
    }
    public class MyTask_S3_IMG_Upload extends AsyncTask<String , Integer, String>{



        @Override
        protected String doInBackground(String... strings) {
            s3.setRegion(Region.getRegion(Regions.US_WEST_2));
            String key=keyedit.getText().toString();
            transferUtility =new TransferUtility(s3,getBaseContext());
            ObjectMetadata metadata=new ObjectMetadata();
            Map<String, String> usermetadata= new HashMap<>();
            usermetadata.put("imgRek",key);

            metadata.setUserMetadata(usermetadata);

            String newfilepath = Environment.getExternalStorageDirectory()+"/FirstCut/subash/abs.txt";



            System.out.println("TransferUtility : "+transferUtility);
            TransferObserver transferObserver=transferUtility.upload("firstcutapplication",key,new File(newfilepath),metadata);
//            TransferObserver transferObserver=transferUtility.upload(
//                    "awsreg",
//                    key,
//                    new File(Path)
//            );
            transferObserver.setTransferListener(new TransferListener(){
                @Override
                public void onStateChanged(int id, TransferState state) {
                    // do something
                    Log.d("log", "state changed. id = "+id+"\tstate = "+state);
                    System.out.println(" state changed");
                }
                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    int percentage = (int) (bytesCurrent/bytesTotal * 100);

                    //Display percentage transfered to user
                    publishProgress(percentage);
                    Log.d("log", "onProgressChanged = "+percentage);
                    System.out.println(" onProgressChanged "+percentage);

                    if(percentage==100){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Main2Activity.this, "FIle Uploaded", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }
                @Override
                public void onError(int id, Exception ex) {
                    // do something
                    Log.d("log", "error in uploading. id = "+id+"\nException = "+ex);
                    System.out.println(" ERROR "+ex);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Main2Activity.this, "Error in uploading..", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            return null;

        }
    }
    public class MyTask_S3_IMG_Down extends AsyncTask<String, Integer, String>
    {

        @Override
        protected String doInBackground(String... strings) {
            String key=keyedit.getText().toString();
            s3.setRegion(Region.getRegion(Regions.US_WEST_2));
            TransferUtility transferUtility = new TransferUtility(s3, getBaseContext());
            TransferObserver observer = transferUtility.download(
                    "awsreg",      /* The bucket to upload to */
                    key,     /* The key for the uploaded object */
                    new File("/storage/emulated/0/AWS/"+key+".jpg")        /* The file where the data to upload exists */
            );
            System.out.println("Tr Util");
            observer.setTransferListener(new TransferListener(){
                @Override
                public void onStateChanged(int id, TransferState state) {
                    // do something
                    Log.d("log", "state changed. id = "+id+"\tstate = "+state);
                    System.out.println(" state changed");
                }
                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             Toast.makeText(Main2Activity.this, "File Downloaded \n Available in internalstorage/aws/", Toast.LENGTH_SHORT).show();
                         }
                     });

                }
                @Override
                public void onError(int id, Exception ex) {
                    System.out.println("EXCEPTION "+ex);
                }
            });

            return null;
        }

    }
    public class MyTask_Listing extends AsyncTask<Integer, String, Integer>{

        @Override
        protected Integer doInBackground(Integer... integers) {
            key_list=new ArrayList<>();
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest().withQueueUrl(result.getQueueUrl());
            ReceiveMessageResult receiveMessageResult =sqsClient.receiveMessage(receiveMessageRequest);
            System.out.println("BowwwwwwwResult"+ receiveMessageResult.getMessages());
            ObjectListing objectListing=s3.listObjects("awsreg");
            for(S3ObjectSummary objectSummary:objectListing.getObjectSummaries()){
                System.out.println("Key :"+objectSummary.getKey().toString());
                key_list.add(objectSummary.getKey().toString());
            }
            System.out.println("The list is  : "+key_list);
          runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  adapter =new ArrayAdapter<String>(Main2Activity.this,R.layout.list_item,R.id.textitem,key_list);

                  listView.setAdapter(adapter);
              }
          });
            return null;
        }
    }

}
