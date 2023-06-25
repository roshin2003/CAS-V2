package com.codebeginner.sovary.fingerprinttest;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.codebeginner.sovary.fingerprinttest.reotrfit.RetrofitService;
import com.codebeginner.sovary.fingerprinttest.reotrfit.User;
import com.codebeginner.sovary.fingerprinttest.reotrfit.UserApi;


import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.github.javafaker.CreditCardType;
import com.github.javafaker.Faker;

public class MainActivity extends AppCompatActivity {

    Button btn_fppin;
    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /* btn_fp = findViewById(R.id.btn_fp);*/
        btn_fppin = findViewById(R.id.btn_fppin);
        try {
            checkBioMetricSupported();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                                "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                Toast.makeText(getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                sendPostRequest();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                //attempt not regconized fingerprint
                Toast.makeText(getApplicationContext(), "Authentication failed",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });
        /*btn_fp.setOnClickListener(view -> {
            BiometricPrompt.PromptInfo.Builder promptInfo = dialogMetric();
            promptInfo.setNegativeButtonText("Cancel");
            biometricPrompt.authenticate(promptInfo.build());
        });*/
        btn_fppin.setOnClickListener(view -> {
            BiometricPrompt.PromptInfo.Builder promptInfo = dialogMetric();
            promptInfo.setDeviceCredentialAllowed(true);
            biometricPrompt.authenticate(promptInfo.build());
        });
    }

    BiometricPrompt.PromptInfo.Builder dialogMetric() {
        //Show prompt dialog
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login")
                .setSubtitle("Log in using your biometric credential");
    }

    //    public static void sendPingRequest(String ipAddress)
//            throws UnknownHostException, IOException
//    {
//        InetAddress geek = InetAddress.getByName(ipAddress);
//        System.out.println("Sending Ping Request to " + ipAddress);
//        if (geek.isReachable(5000))
//            System.out.println("Host is reachable");
//        else
//            System.out.println("Sorry ! We can't reach to this");
//}


    public  void locator() {
        final double MIN_LATITUDE = -90.0;
        final double MAX_LATITUDE = 90.0;
        final double MIN_LONGITUDE = -180.0;
        final double MAX_LONGITUDE = 180.0;
        Random random = new Random();

        latitude = generateRandomCoordinate(MIN_LATITUDE, MAX_LATITUDE, random);
        longitude = generateRandomCoordinate(MIN_LONGITUDE, MAX_LONGITUDE, random);


    }

    private static double generateRandomCoordinate(double min, double max, Random random) {
        return min + (max - min) * random.nextDouble();
    }
    //must running android 6
    public static int Generator(String cardNumber,double latitude,double longitude) {
//        String cardNumber = "123456789012"; // Replace with your 12-digit card number
//        double latitude = 37.7749; // Replace with your latitude
//        double longitude = -122.4194; // Replace with your longitude

        // Get current timestamp
        long currentTime = new Date().getTime();

        // Concatenate card number, time, latitude, and longitude
        String input = cardNumber + currentTime + latitude + longitude;

        // Hash the input using MD5
        String hashedInput = hashMD5(input);

        // Take the first 3 characters of the hashed input
        assert hashedInput != null;
        String randomDigits = hashedInput.substring(0, 3);

        // Convert the hexadecimal digits to decimal
        int randomNumber = Integer.parseInt(randomDigits, 16);

        // Ensure the generated number is within the desired range (100 - 999)
        randomNumber = (randomNumber % 900) + 100;

        return randomNumber;
    }

    // Hashes the input string using MD5 algorithm
    private static String hashMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());

            // Convert the byte array to a hexadecimal string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }
    public void sendPostRequest() {
        // Specify the API endpoint URL
        RetrofitService retrofitService = new RetrofitService();
        UserApi usrApi = retrofitService.getRetrofit().create(UserApi.class);
        // Create a new User object with the necessary data
//        Call<User> call = userService.addUser(user);
        User usr = new User();
        Faker faker = new Faker();

        String creditCardNumber = faker.finance().creditCard();
        locator();
        int cvv = Integer.parseInt(faker.number().digits(3));
        usr.setLocationLat(Double.toString(longitude));
        usr.setLocationLon(Double.toString(latitude));
        usr.setCardNumber(creditCardNumber);
        String strippedNumber = creditCardNumber.replaceAll("-", "");
        long cardNumber = Long.parseLong(strippedNumber);
        usr.setStaticCvv(cvv);
        usr.setSessionNumber(Generator(strippedNumber,longitude,latitude));
        Call<ResponseEntity<String>> call = usrApi.addUser(usr);
        call.enqueue(new Callback<ResponseEntity<String>>() {
            @Override
            public void onResponse(Call<ResponseEntity<String>> call, Response<ResponseEntity<String>> response) {
                if (!response.isSuccessful()) {
//                    .isSuccessful()

                    // Handle unsuccessful response
                    Toast.makeText(getApplicationContext(), "Save failed: " + response.message(), Toast.LENGTH_SHORT).show();
                } else {//.isSuccessful()
                    // Handle successful response
                    Toast.makeText(getApplicationContext(), "Save successful!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseEntity<String>> call, Throwable t) {
                // Handle failure
                Toast.makeText(getApplicationContext(), "Save failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


        void checkBioMetricSupported() throws IOException {
        BiometricManager manager = BiometricManager.from(this);
        String info;
        switch (manager.canAuthenticate(BIOMETRIC_WEAK | BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                String url="heyy";
////                String ipAddress = "192.168.0.244:8082";
//                sendPingRequest(ipAddress);
//                info = "App can authenticate using biometrics.";
                Log.e("Called2",url);
                enableButton(true);
//                sendPostRequest();
                Log.e("Called3",url);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                info = "No biometric features available on this device.";
                enableButton(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                info = "Biometric features are currently unavailable.";
                enableButton(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                info = "Need register at least one finger print";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    enableButton(false, true);
                }
                break;
            default:
                info = "Unknown cause";
                enableButton(false);
        }
        /*TextView txinfo =  findViewById(R.id.tx_info);
        txinfo.setText(info);*/
    }

    void enableButton(boolean enable) {
        //btn_fp.setEnabled(enable);
        btn_fppin.setEnabled(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    void enableButton(boolean enable, boolean enroll) {
        enableButton(enable);

        if (!enroll) return;
        // Prompts the user to create credentials that your app accepts.
        //Open settings to set credential
        final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
        enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
        startActivity(enrollIntent);
    }
}