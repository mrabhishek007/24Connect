package project.android.com.connect24;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button registerNow,loginNow;
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

       registerNow =  findViewById(R.id.registerbutton);

       loginNow = findViewById(R.id.start_activity_loginbutton);

       //When register button is pressed

       registerNow.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view)
           {
               startActivity(new Intent(StartActivity.this,RegisterActivity.class));
           }
       });

       //When register button is clicked

        loginNow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(StartActivity.this,LoginActivity.class));

            }
        });

    }
}
