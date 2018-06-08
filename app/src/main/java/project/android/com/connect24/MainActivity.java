package project.android.com.connect24;

import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity
{
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private DatabaseReference mUserDatabase;
    private ProgressDialog mLoginProgressDialog;
    private InternetAvailabilityChecker mInternetAvailabilityChecker;

    //For Lockscreen validation

    private static final String KEY_NAME = "my_key";
    private static final byte[] SECRET_BYTE_ARRAY = new byte[] {1, 2, 3, 4, 5, 6};
    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1;
    private static final int ANDROID_SECURITY_SETTING = 2;
    /**
     * If the user has unlocked the device Within the last this number of seconds,
     * it can be considered as an authenticator.
     */
    private static final int AUTHENTICATION_DURATION_SECONDS = 30;
    private KeyguardManager mKeyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        /**
        if(currentUser!=null)
        {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            {
                validateLockScreen();
            }
        }*/

        mToolbar = findViewById(R.id.mainpage_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("24Connect");

        //Tabs
       mViewPager =  findViewById(R.id.view_pager_main);
       SectionPageAdapter mSectionPageAdapter = new SectionPageAdapter(this,getSupportFragmentManager());
       mViewPager.setAdapter(mSectionPageAdapter);
       mTabLayout =  findViewById(R.id.tabPager_main);
       mTabLayout.setupWithViewPager(mViewPager);




    }

    public void onStart()
    {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        if(currentUser==null)
        {
            //When user is not logged-in
            sendToStart();
        }
        else
        {
            //If user is already logged_in

            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(mAuth.getCurrentUser().getUid());
            mUserDatabase.child("online").setValue(""+true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
       MenuInflater menuInflater =  getMenuInflater();
       menuInflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        int menu_item_id= item.getItemId();

        if(menu_item_id==R.id.main_settings)
        {
            Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        }

        if(menu_item_id == R.id.main_all_users)
        {
            Intent intent = new Intent(MainActivity.this,AllUserActivity.class);
            startActivity(intent);
        }

        if(menu_item_id==R.id.main_logout)
        {
            //When signOut is pressed
            mLoginProgressDialog = new ProgressDialog(this);
            mLoginProgressDialog.setTitle("Logging Out");
            mLoginProgressDialog.setMessage("Please wait...");
            mLoginProgressDialog.setCancelable(false);
            mLoginProgressDialog.show();

            //Removing device token when signout is pressed

            mUserDatabase.child("device_token").removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
            {
                @Override
                public void onSuccess(Void aVoid)
                {
                    mUserDatabase.child("online").setValue(""+false);
                    mUserDatabase.child("last_seen").setValue(ServerValue.TIMESTAMP);

                    mAuth.signOut();
                    mLoginProgressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Logout Sucessfully ", Toast.LENGTH_SHORT).show();
                    sendToStart();
                }
            });


            //CHECK WHETHER INTERNET IS WORKING OR NOT
//
//           final InternetAvailabilityChecker ac =  InternetAvailabilityChecker.init(this);
//            ac.addInternetConnectivityListener(new InternetConnectivityListener() {
//                @Override
//                public void onInternetConnectivityChanged(boolean isConnected)
//                {
//                    if(isConnected)
//                    {
//
//                    }
//                    else
//                    {
//                        //When no internet connection!
//
//                        Toast.makeText(MainActivity.this, "No Internet connection ! ", Toast.LENGTH_SHORT).show();
//                        mLoginProgressDialog.dismiss();
//                        ac.removeAllInternetConnectivityChangeListeners();
//                    }
//
//                }
//            });

        }
        return true;
    }

    private void sendToStart()
    {

        startActivity(new Intent(MainActivity.this,StartActivity.class));
        finish();
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        mUserDatabase.child("online").setValue(""+false);
        mUserDatabase.child("last_seen").setValue(ServerValue.TIMESTAMP);
    }

            // ANDROID CONFIRM CRDENTIAL API IMPLEMENTATION


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void validateLockScreen()
    {
            mKeyguardManager   =  (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

            if (!mKeyguardManager.isKeyguardSecure())
            {

                // Show a message that the user hasn't set up a lock screen.

//            Toast.makeText(MainActivity.this, "Secure lock screen hasn't set up.\n"
//                    + "Go to 'Settings -> Security -> Screenlock' to set up a lock screen", Toast.LENGTH_SHORT).show();

                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.mainactivity_ll), "Setup Lockscreen!", Snackbar.LENGTH_LONG)
                        .setAction("SETUP", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                                try {

                                    //Start activity for result
                                    startActivityForResult(intent, ANDROID_SECURITY_SETTING);
                                } catch (Exception ex) {
                                    //If app is unable to find any Security settings then user has to set screen lock manually
                                    Toast.makeText(MainActivity.this, "Unable to find security setting", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                // Changing message text color
                snackbar.setActionTextColor(Color.RED);
                // Changing action button text color
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.YELLOW);
                snackbar.show();
                return;

            } // Check whether device has lock enabled or not
             else
                 {

                //If lock screen is already setup

                createKey();

                // Test to encrypt something. It might fail if the timeout expired (30s).
                tryEncrypt();
              }
    }

    private void createKey()
    {
        // Generate a key to decrypt payment credentials, tokens, etc.
        // This will most likely be a registration step for the user when they are setting up your app.

        try
        {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        // Require that the user has unlocked in the last 30 seconds
                        .setUserAuthenticationValidityDurationSeconds(AUTHENTICATION_DURATION_SECONDS)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }
            keyGenerator.generateKey();
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException
                | InvalidAlgorithmParameterException | KeyStoreException
                | CertificateException | IOException e) {
            throw new RuntimeException("Failed to create a symmetric key", e);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean tryEncrypt()
    {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_NAME, null);
            Cipher cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // Try encrypting something, it will only work if the user authenticated within
            // the last AUTHENTICATION_DURATION_SECONDS seconds.

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            cipher.doFinal(SECRET_BYTE_ARRAY);

            // If the user has recently authenticated, you will reach here.
            showAlreadyAuthenticated();
            return true;

        } catch (UserNotAuthenticatedException e) {
            // User is not authenticated, let's authenticate with device credentials.
            showAuthenticationScreen();
            return false;

        } catch (KeyPermanentlyInvalidatedException e) {
            // This happens if the lock screen has been disabled or reset after the key was
            // generated after the key was generated.
            Toast.makeText(this, "Keys are invalidated after created. Retry the purchase\n"
                            + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            return false;
        }
        catch (BadPaddingException | IllegalBlockSizeException | KeyStoreException |
                CertificateException | UnrecoverableKeyException | IOException
                | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }


    }

    private void showAuthenticationScreen() {
        // Create the Confirm Credentials screen. You can customize the title and description. Or
        // we will provide a generic one for you if you leave it null
        Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent("Unlock 24Connect ", "Confirm your screen lock Pin/ Pattern/ Password ");
        if (intent != null)
        {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        }
    }

    private void showAlreadyAuthenticated()
    {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            // Challenge completed, proceed with using cipher
            if (resultCode == RESULT_OK) {
                if (tryEncrypt())
                {
                    final Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.mainactivity_ll), "Login Successful !", Snackbar.LENGTH_LONG);
                    // Changing message text color
                    // Changing action button text color
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    sbView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    snackbar.show();
                }
            } else {
                // The user canceled or didn’t complete the lock screen
                // operation. Go to error/cancellation flow.
                finish();
            }
        }

        if(requestCode ==ANDROID_SECURITY_SETTING)
        {
            if(mKeyguardManager.isDeviceSecure())
            {
                final Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.mainactivity_ll), "Lockscreen Successfully Setup ", Snackbar.LENGTH_LONG);
                // Changing message text color
                snackbar.setActionTextColor(Color.RED);
                // Changing action button text color
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.YELLOW);
                snackbar.show();
                snackbar.setAction("Close", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
            }
            else
            {
                final Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.mainactivity_ll), "Screen lock canceled !", Snackbar.LENGTH_LONG);
                // Changing message text color
                snackbar.setActionTextColor(Color.RED);
                // Changing action button text color
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.YELLOW);
                snackbar.show();
                snackbar.setAction("Close", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
        }
    }
   }//onActivityResult
}
