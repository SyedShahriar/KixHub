package syedshahriar.com.KixHub;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class FiltersActivity extends AppCompatActivity{

    private static final String TAG = "FiltersActivity";

    //widgets
    private Button mSave;
    private EditText mCity,mStateProvince,mCountry;
    private ImageView mBackArrow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        mSave = findViewById(R.id.btnSave);
        mCity = findViewById(R.id.input_city);
        mStateProvince = findViewById(R.id.input_state_province);
        mCountry = findViewById(R.id.input_country);
        mBackArrow = findViewById(R.id.backArrow);
        init();
    }

    private void init(){
        getFilterPreferences();

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: saving...");
                SharedPreferences preferences = PreferenceManager.
                        getDefaultSharedPreferences(FiltersActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                Log.d(TAG,"onClick: city: " + mCity.getText().toString());
                editor.putString(getString(R.string.preferences_city),mCity.getText().toString());
                editor.commit();

                Log.d(TAG,"onClick: state_province: " + mStateProvince.getText().toString());
                editor.putString(getString(R.string.preferences_state_province),mStateProvince.getText().toString());
                editor.commit();

                Log.d(TAG,"onClick: country: " + mCountry.getText().toString());
                editor.putString(getString(R.string.preferences_country),mCountry.getText().toString());
                editor.commit();


            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: navigating back");
                finish();
            }
        });
    }

    private void getFilterPreferences(){
        Log.d(TAG,"getFilterPreferences: retrieving user preferences");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String country = preferences.getString(getString(R.string.preferences_country),"");
        String city = preferences.getString(getString(R.string.preferences_city),"");
        String state_province = preferences.getString(getString(R.string.preferences_state_province),"");

        mCountry.setText(country);
        mStateProvince.setText(state_province);
        mCity.setText(city);
    }
}
