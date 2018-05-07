package vibe.remote2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.hitomi.cmlibrary.CircleMenu;
import com.hitomi.cmlibrary.OnMenuSelectedListener;

import static java.lang.Thread.sleep;


public class MenuActivity extends AppCompatActivity {


    private CircleMenu circleMenu;
    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        circleMenu = findViewById(R.id.circle_menu);
        circleMenu.setMainMenu(Color.parseColor("#ffffff"),R.mipmap.ic_vibe3,R.mipmap.ic_vibe3)
        .addSubMenu(Color.parseColor("#ffffff"),R.mipmap.ic_bulblarge)
                .addSubMenu(Color.parseColor("#ffffff"),R.mipmap.ic_blurcirclelarge);


    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        circleMenu.openMenu();
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onBackPressed() {
        circleMenu.closeMenu();

    }

    @Override
    public void onResume(){
        super.onResume();
        circleMenu.setOnMenuSelectedListener(new OnMenuSelectedListener() {

            @Override
            public void onMenuSelected(int i) {
                if (i==0) {
                    mHandler.postDelayed(mUpdateTimeTask, 900);

                }
                if (i==1) {
                    /*CharSequence text = "Send to Chameleon control..";
                    Toast toast = Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT);
                    toast.show();*/
                    mHandler.postDelayed(mUpdateTimeTask2,900);
                }
            }

            private Runnable mUpdateTimeTask = new Runnable() {
                public void run() {
                    Intent ledIntent = new Intent(MenuActivity.this,
                            LEDControl.class);
                    startActivity(ledIntent);
                }
            };

            private Runnable mUpdateTimeTask2 = new Runnable() {
                @Override
                public void run() {
                    Intent chamIntent = new Intent(MenuActivity.this,Chameleon.class);
                    startActivity(chamIntent);
                }
            };

        });
    }


}
