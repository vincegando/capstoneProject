package capstone.cs189.com.smartnetwork;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageView imageView = (ImageView) findViewById(R.id.image_background);
        imageView.setColorFilter(Color.rgb(60, 60, 60), PorterDuff.Mode.MULTIPLY);
        Picasso.with(getApplicationContext()).load(R.drawable.cables).fit().centerCrop().into(imageView);

    }
}
