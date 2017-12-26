package com.example.user.awsex.DynamoDb;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.example.user.awsex.R;

import java.util.ArrayList;
import java.util.List;

public class DynamoDB extends AppCompatActivity {

    AmazonDynamoDBClient ddb;
    Button submit,list;
    EditText bn,rat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamo_db);

        CognitoCachingCredentialsProvider credentialsProvider=new CognitoCachingCredentialsProvider(getApplicationContext(),"us-west-2:20e04e1d-cd9d-46ca-9305-93fe4f13f312", Regions.US_WEST_2);

        ddb=new AmazonDynamoDBClient(credentialsProvider);
        ddb.setRegion(Region.getRegion(Regions.US_WEST_2));

        DynamoDBMapper mapper=new DynamoDBMapper(ddb);

        submit=(Button)findViewById(R.id.submit);
        list=(Button)findViewById(R.id.list);
        bn=(EditText)findViewById(R.id.BookName);
        rat=(EditText)findViewById(R.id.Ratings);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DDB_MyTask().execute();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Creating table");
                new Create_table_Task().execute();

            }
        });


    }
    public class DDB_MyTask extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... strings) {
            ListTablesResult listTablesResult=ddb.listTables();
            System.out.println("List of Tables : "+listTablesResult);

            return null;
        }
    }
    public class Create_table_Task extends  AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... strings) {

            String book=bn.getText().toString().replace(" ","");
            String rating=rat.getText().toString();

            System.out.println("Books :  "+book+ "Ratings : "+rating);


            List<AttributeDefinition> attributeDefinitions= new ArrayList<>();
            attributeDefinitions.add(new AttributeDefinition().withAttributeName("Ratings").withAttributeType(ScalarAttributeType.N));
//            attributeDefinitions.add(new AttributeDefinition().withAttributeName("BookName").withAttributeType(ScalarAttributeType.S));

            List<KeySchemaElement> keySchemaElements=new ArrayList<>();
            keySchemaElements.add(new KeySchemaElement().withAttributeName("Ratings").withKeyType(KeyType.HASH));

            CreateTableRequest request=new CreateTableRequest().withTableName(book).withAttributeDefinitions(attributeDefinitions).withKeySchema(keySchemaElements)
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

            try{
                CreateTableResult result=ddb.createTable(request);
                System.out.println("Result Of Table Creation : "+ result.getTableDescription());
            }catch (final ResourceInUseException e){
                System.out.println("Error : "+ e.getErrorMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DynamoDB.this, e.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            return null;
        }
    }
}
