package com.example.androidmultiplepermissionruntime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final int CAMERA_REQUEST = 102;
    Button capture_image,share_image;
    ImageView preview_image;
    String  file_path=null;
    String[] all_permission={Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        capture_image=findViewById(R.id.capture_image);
        share_image=findViewById(R.id.share_image);
        preview_image=findViewById(R.id.preview_image);


        capture_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=23){
                    if(checkPermission(all_permission)){
                        captureImage();
                    }
                    else{
                        requestPermission(all_permission);
                    }
                }
                else{
                    captureImage();
                }

            }
        });

        share_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //now for sharing file i need to create file provider for android version greater than noughat let's do it
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,"Demo Title");
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
                    Uri path=FileProvider.getUriForFile(MainActivity.this,"com.example.androidmultiplepermissionruntime",new File(file_path));
                    intent.putExtra(Intent.EXTRA_STREAM,path);
                }
                else{
                    intent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(new File(file_path)));
                }
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("plain/*");
                startActivity(intent);


                //Everything WOrking


            }
        });
    }

    private void captureImage(){

        //Permisson Working Now
        //Save our Capture Image Now and Later i can share image
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CAMERA_REQUEST);

    }

    private boolean checkPermission(String[] permisson){
        for(int i=0;i<permisson.length;i++){
            int result= ContextCompat.checkSelfPermission(MainActivity.this,permisson[i]);
            if(result== PackageManager.PERMISSION_GRANTED){
                continue;
            }
            else{
                return false;
            }
        }
        return true;
    }

    private void requestPermission(String[] permission){
        ActivityCompat.requestPermissions(MainActivity.this,permission,PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this, "Permission Successfull", Toast.LENGTH_SHORT).show();
                    captureImage();

                }
                else{
                    Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==CAMERA_REQUEST){
                if(data!=null){
                    Bitmap image= (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG,100,bytes);
                    File dest=new File(Environment.getExternalStorageDirectory(),"temp_img.jpg");
                    if(dest.exists()){
                        dest.delete();
                    }

                    FileOutputStream fo;
                    try{
                        fo=new FileOutputStream(dest);
                        fo.write(bytes.toByteArray());
                        fo.close();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }

                    preview_image.setImageBitmap(image);

                    file_path=dest.getPath();
                    //capture working let's share image
                }
            }
        }
    }
}
