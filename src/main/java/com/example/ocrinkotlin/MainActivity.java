package com.example.ocrinkotlin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity  {

    private final OkHttpClient client = new OkHttpClient();
    private ImageView imageView;
    private EditText convertedText;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST=22;

    //instance for firebase storage and storageReference

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    //print button





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //init

        Button btnUpload=findViewById(R.id.btnUpload);
        convertedText=findViewById(R.id.convertedText);


        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();

        //print button



        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });



    }
    //select image method
    public void selectImage(){
        //implicit intent
        Intent intent=new Intent();
        intent.setType("image/*");
        intent .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select image "),PICK_IMAGE_REQUEST);
    }
    //upload image method
    public void uploadImage(){
        if(filePath != null){

            ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setTitle("uploading....");
            progressDialog.show();

            StorageReference ref=storageReference.child("images/image");

            //adding listener on upload
            //or failure of image

            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //Image uploaded successfully
                    //Dismiss dialog
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Image Uploaded..", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    //Error ,image not uploaded

                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    //progress listener for dialog
                    //progress on the dialog box

                    double progress=(100.0 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded" + (int)progress + "%");
                }
            });

            //point to the root reference
            StorageReference storageReference=FirebaseStorage.getInstance().getReference();
            StorageReference dateRef=storageReference.child("images/image");
            dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>(){
                @Override
                public void onSuccess(Uri uri){
                    Toast.makeText(MainActivity.this, uri.toString(), Toast.LENGTH_SHORT).show();
                    int SDK_INT= Build.VERSION.SDK_INT;
                    if(SDK_INT>8){
                        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                    }try{
                        sendPost(uri.toString());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });

        }
    }





    private void sendPost(String imageUrl) throws Exception{

        //form parameter

        RequestBody formBody=new FormBody.Builder()
                .add("Language","eng")
                .add("isOverlayRequired","false")
                .add("url",imageUrl)
                .add("iscreatesearchablepdf","false")
                .add("issearchablepdfhidetextlayer","false")
                .build();
        okhttp3.Request request=new Request.Builder()
                .url("https://api.ocr.space/parse/image")
                .addHeader("User-Agent","OkHttp Bot")
                .addHeader("apikey","K85335335388957")
                .post(formBody)
                .build();

        try(Response response=client.newCall(request).execute()){
            if(!response.isSuccessful()) throw new IOException("Unexpected code "+response);

            //get response body

            String res=response.body().string();
            System.out.println(res);
            JSONObject obj=new JSONObject(res);
            JSONArray jsonArray=(JSONArray) obj.get("ParsedResults");
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                convertedText.setText(jsonObject.get("ParsedText").toString());
            }

        }

    }
    @Override
    protected  void onActivityResult(int requestCode ,int resultCode,Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        //checking request code and result code.
        //if request code is PICK_IMAGE_REQUEST and
        //result code is RESULT_OK
        //then set image in the image view

        if(requestCode==PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() !=null){
            filePath=data.getData();
            try{
                Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);

                imageView.setImageBitmap(bitmap);
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

}
