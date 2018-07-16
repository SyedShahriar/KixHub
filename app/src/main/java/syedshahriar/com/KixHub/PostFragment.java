package syedshahriar.com.KixHub;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import syedshahriar.com.KixHub.models.Post;
import syedshahriar.com.KixHub.util.RotateBitmap;
import syedshahriar.com.KixHub.util.UniversalImageLoader;


public class PostFragment extends Fragment implements SelectPhotoDialog.onPhotoSelectedListener{

    private static final String TAG = "PostFragment";

    @Override
    public void getImagePath(Uri imagePath) {
        Log.d(TAG,"getImagePath: setting the image to imageview");
        UniversalImageLoader.setImage(imagePath.toString(),mPostImage);
        //assign to global variable
        mSelectedBitmap = null;
        mSelectedUri = imagePath;
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        Log.d(TAG,"getImageBitMap: setting the image to imageview");

        mPostImage.setImageBitmap(bitmap);
        //assign to global variable
        mSelectedUri = null;
        mSelectedBitmap = bitmap;

    }

    //Widgets
    private ImageView mPostImage;
    private EditText mTitle, mDescription, mPrice, mCountry, mStateProvince, mCity, mContactEmail;
    private Button mPost;
    private ProgressBar mProgressBar;

    //vars
    private Bitmap mSelectedBitmap;
    private Uri mSelectedUri;
    private byte[] mUploadBytes;
    private double mProgress = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post,container,false);
        mPostImage = view.findViewById(R.id.post_image);
        mTitle = view.findViewById(R.id.input_title);
        mDescription = view.findViewById(R.id.input_description);
        mPrice = view.findViewById(R.id.input_price);
        mCountry = view.findViewById(R.id.input_country);
        mStateProvince = view.findViewById(R.id.input_state_province);
        mCity = view.findViewById(R.id.input_city);
        mContactEmail = view.findViewById(R.id.input_email);
        mPost = view.findViewById(R.id.btn_post);
        mProgressBar = view.findViewById(R.id.progressBar);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        init();

        return view;
    }

    private void init(){

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog to choose new photo");
                SelectPhotoDialog dialog = new SelectPhotoDialog();
                dialog.show(getFragmentManager(),getString(R.string.dialog_select_photo));
                dialog.setTargetFragment(PostFragment.this,10);
            }
        });

        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: attempting to post");
                if(!isEmpty(mTitle.getText().toString())
                        && !isEmpty(mDescription.getText().toString())
                        && !isEmpty(mPrice.getText().toString())
                        && !isEmpty(mCountry.getText().toString())
                        && !isEmpty(mStateProvince.getText().toString())
                        && !isEmpty(mCity.getText().toString())
                        && !isEmpty(mContactEmail.getText().toString())){
                    //Have bitmap but no uri
                    if (mSelectedBitmap!=null && mSelectedUri==null){
                        uploadNewPhoto(mSelectedBitmap);
                    }
                    //Have uri but no bitmap
                    else if (mSelectedBitmap==null && mSelectedUri!=null){
                        uploadNewPhoto(mSelectedUri);
                    }
                }
                else{
                    Toast.makeText(getActivity(),"Please Ensure all fields are complete",Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    private void uploadNewPhoto(Bitmap bitmap){
        Log.d(TAG,"uploading new image bitmap to storage.");
        BackgroundImageResize resize = new BackgroundImageResize(bitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    private void uploadNewPhoto(Uri imagePath){
        Log.d(TAG,"uploadNewPhoto: uploading new image Uri to storage.");
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imagePath);
    }

    public class BackgroundImageResize extends AsyncTask<Uri,Integer,byte[]>{
        Bitmap mbitmap;

        public BackgroundImageResize(Bitmap bitmap) {
            if (bitmap!=null){
                this.mbitmap = bitmap;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getActivity(), "Compressing Image", Toast.LENGTH_SHORT).show();
            showProgressBar();
        }

        @Override
        protected byte[] doInBackground(Uri... uris) {
            Log.d(TAG,"doInBackground: started");
            if(mbitmap==null){
                try{
                    RotateBitmap rotateBitmap = new RotateBitmap();
                    mbitmap = rotateBitmap.HandleSamplingAndRotationBitmap(getActivity(),uris[0]);
                }
                catch(IOException e){
                    Log.e(TAG,"doInBackground: IOException: " + e.getMessage());
                }
            }
            byte [] bytes = null;
            Log.d(TAG,"doInBackground: mb before compression: " + mbitmap.getByteCount()/1000000);
            bytes = getBytesFromBitMap(mbitmap,100);
            Log.d(TAG,"doInBackground: mb before compression: " + bytes.length/1000000);
            return bytes;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            mUploadBytes = bytes;
            hideProgressBar();
            //execute upload task
            executeUploadTask();
        }
    }

    private void executeUploadTask(){
        Toast.makeText(getActivity(),"Uploading Image",Toast.LENGTH_SHORT);
        final String postId = FirebaseDatabase.getInstance().getReference().push().getKey();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("posts/users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid() +
                "/" + postId + "/post_image");
        UploadTask uploadTask = storageReference.putBytes(mUploadBytes);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getActivity(),"Post was Successful",Toast.LENGTH_SHORT).show();
                //Insert the download uri into the firebase database
                Uri firebaseUri = taskSnapshot.getDownloadUrl();
                Log.d(TAG,"OnSuccess: Firebase download Uri: " + firebaseUri.toString());
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                Post post = new Post();
                post.setImage(firebaseUri.toString());
                post.setCity(mCity.getText().toString());
                post.setContact_email(mContactEmail.getText().toString());
                post.setCountry(mCountry.getText().toString());
                post.setDescription(mDescription.getText().toString());
                post.setPrice(mPrice.getText().toString());
                post.setState_province(mStateProvince.getText().toString());
                post.setPost_id(postId);
                post.setTitle(mTitle.getText().toString());
                post.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

                reference.child(getString(R.string.node_posts))
                        .child(postId)
                        .setValue(post);
                resetFields();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(),"Could not upload Image",Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double currentProgess = (100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                if(currentProgess>mProgress+15){
                    mProgress = (100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "onProgress: Upload is " + mProgress + "& done");
                    Toast.makeText(getActivity(),mProgress + "%", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static byte[] getBytesFromBitMap(Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,quality,stream);
        return stream.toByteArray();
    }

    private void resetFields(){
        UniversalImageLoader.setImage("", mPostImage);
        mTitle.setText("");
        mDescription.setText("");
        mPrice.setText("");
        mCountry.setText("");
        mStateProvince.setText("");
        mCity.setText("");
        mContactEmail.setText("");
    }

    private void showProgressBar(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideProgressBar(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Return true if the @param is null
     * @param string
     * @return
     */
    private boolean isEmpty(String string){
        return string.equals("");
    }

}
