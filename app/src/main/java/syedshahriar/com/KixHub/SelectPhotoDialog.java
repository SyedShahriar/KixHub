package syedshahriar.com.KixHub;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.net.URI;

public class SelectPhotoDialog extends DialogFragment {

    private static final String TAG = "SelectPhotoDialog";
    private static final int PICKFILE_REQUEST_CODE = 1234;
    private static final int CAMERA_REQUEST_CODE = 4321;

    public interface onPhotoSelectedListener{
        void getImagePath (Uri imagePath);
        void getImageBitmap (Bitmap bitmap);
    }

    onPhotoSelectedListener mPhotoSelectedListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_selectphoto,container,false);
        TextView selectPhoto = view.findViewById(R.id.dialogChoosePhoto);
        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: accessing phones memory");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,PICKFILE_REQUEST_CODE);
            }
        });

        TextView takePhoto = view.findViewById(R.id.dialogOpenCamera);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: starting camera");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,CAMERA_REQUEST_CODE);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Results when selecting a new image from memory

        if(requestCode==PICKFILE_REQUEST_CODE && resultCode== Activity.RESULT_OK){
            Uri selectedImageUri = data.getData();
            Log.d(TAG,"onActivityResult: image uri: "+ selectedImageUri);

            //send image uri to PostFragment & dismiss dialog
            mPhotoSelectedListener.getImagePath(selectedImageUri);
            getDialog().dismiss();

        }
        //Results when taking a new photo with camera
        else if(requestCode==CAMERA_REQUEST_CODE && resultCode== Activity.RESULT_OK){
            Log.d(TAG,"onActivityResult: done taking new photo");
            Bitmap bitmap;
            bitmap = (Bitmap) data.getExtras().get("data");

            //send the bitmap to postfragment and dismiss dialog
            mPhotoSelectedListener.getImageBitmap(bitmap);
            getDialog().dismiss();

        }
    }

    @Override
    public void onAttach(Context context) {
        try{
            mPhotoSelectedListener = (onPhotoSelectedListener) getTargetFragment();
        }
        catch (ClassCastException e){
            Log.e(TAG,"onAttach: ClassCastException: " + e.getMessage());

        }
        super.onAttach(context);
    }
}
