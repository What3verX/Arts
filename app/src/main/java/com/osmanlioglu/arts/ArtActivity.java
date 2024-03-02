package com.osmanlioglu.arts;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.osmanlioglu.arts.databinding.ActivityArtBinding;


import java.io.ByteArrayOutputStream;


public class ArtActivity extends AppCompatActivity {

    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        launcherRegister();
        database = openOrCreateDatabase("artDatas",MODE_PRIVATE,null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.equals("new")){
            //user wants to add a new art

            binding.nameArtist.setText("");
            binding.name.setText("");
            binding.year.setText("");
            binding.imageView2.setImageResource(R.drawable.slc);
            binding.save.setVisibility(View.VISIBLE);
        }
        else {
            //user wants to see an existing art

            int itemId = intent.getIntExtra("itemId",1);
            binding.save.setVisibility(View.INVISIBLE);
            binding.imageView2.setEnabled(false);
            binding.year.setFocusable(false);
            binding.name.setFocusable(false);
            binding.nameArtist.setFocusable(false);


            try {


                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", new String[] {String.valueOf(itemId)});
                int nameIx = cursor.getColumnIndex("name");
                int yearIx = cursor.getColumnIndex("year");
                int artistIx = cursor.getColumnIndex("artistname");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    binding.nameArtist.setText(cursor.getString(artistIx));
                    binding.name.setText(cursor.getString(nameIx));
                    binding.year.setText(cursor.getString(yearIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView2.setImageBitmap(bitmap);

                }

                cursor.close();

            }catch (Exception e){
                e.printStackTrace();
            }
            

        }

    }



    public void addPicture(View view){

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
            //READ_MEDIA_IMAGES
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)){

                    Snackbar.make(view,"We need to access your gallery for let you choose an image",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();

                }
                else{
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }

            }

            else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }


        }

        else {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){

                    Snackbar.make(view,"We need to access your gallery for let you choose an image",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();

                }
                else{
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }

            }

            else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }




    }


    public void save(View view) {
        String name = binding.name.getText().toString();
        String artistName = binding.nameArtist.getText().toString();
        String year = binding.year.getText().toString();

        // ImageView'a tıklanıp tıklanmadığını ve fotoğrafın seçilip seçilmediğini kontrol et
        boolean isImageViewClicked = binding.imageView2.isClickable();
        boolean isImageSelected = (selectedImage != null);

        if (name.isEmpty() || artistName.isEmpty() || year.isEmpty() || !(isImageViewClicked || isImageSelected)) {
            Toast.makeText(this, "Please enter the information!", Toast.LENGTH_LONG).show();
        } else {
            // Save işlemi devam eder.
            Bitmap smallerImage = makeSmallerImage(selectedImage,300);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            smallerImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
            byte [] bytes = outputStream.toByteArray();

            try {


                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, name VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)");
                SQLiteStatement sqLiteStatement = database.compileStatement("INSERT INTO arts (name, artistname, year, image) VALUES (?,?,?,?)");

                sqLiteStatement.bindString(1,name);
                sqLiteStatement.bindString(2,artistName);
                sqLiteStatement.bindString(3,year);
                sqLiteStatement.bindBlob(4,bytes);
                sqLiteStatement.execute();
                //we used statement to take the datas after user inputs them

            }catch (Exception e){
                e.printStackTrace();
            }

            Intent intent = new Intent(ArtActivity.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

            }






    public Bitmap makeSmallerImage(Bitmap image,int maxSize){

        int h = image.getHeight();
        int w = image.getWidth();


        float ratio = (float) h/w;

        if (ratio>1){
            int old_h = h;
            h = maxSize;
            w = w/(old_h/maxSize);
        }
        else {
            int old_w = w;
            w = maxSize;
            h = h/(old_w/maxSize);

        }


        return Bitmap.createScaledBitmap(image,w,h,true);

    }


    public void launcherRegister(){

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode()==RESULT_OK){
                    Intent intent = o.getData();
                    if (intent!=null){
                        Uri uri = intent.getData();
                        try {
                            ImageDecoder.Source source = ImageDecoder.createSource(ArtActivity.this.getContentResolver(),uri);
                            selectedImage = ImageDecoder.decodeBitmap(source);
                            binding.imageView2.setImageBitmap(selectedImage);


                        }catch (Exception e){
                            e.printStackTrace();


                        }
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                if (o){
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }
                else {
                    Toast.makeText(ArtActivity.this, "Permission Needed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void backArrowClicked(View view){

        Intent intent = new Intent(ArtActivity.this,MainActivity.class);
        startActivity(intent);

    }


}