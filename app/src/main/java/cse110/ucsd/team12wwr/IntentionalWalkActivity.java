package cse110.ucsd.team12wwr;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cse110.ucsd.team12wwr.clock.DeviceClock;
import cse110.ucsd.team12wwr.clock.IClock;
import cse110.ucsd.team12wwr.firebase.DaoFactory;
import cse110.ucsd.team12wwr.firebase.Walk;
import cse110.ucsd.team12wwr.firebase.WalkDao;

public class IntentionalWalkActivity extends AppCompatActivity {
    private static final String TAG = "IntentionalWalkActivity";

    private final int HEIGHT_FACTOR = 12;
    private final double STRIDE_CONVERSION = 0.413;
    private final int MILE_FACTOR = 63360;
    private static int LAUNCH_SECOND_ACTIVITY = 1;

    private TextView stopwatchText, stepsText, distanceText;
    private AsyncTaskRunner runner;
    private int timeWhenPaused, timeElapsed;
    private double strideLength;
    private String routeTitle;
    private boolean fromActivity;

    private int temporaryNumSteps;

    private IClock clock;
    private String userEmail;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intentional_walk);
        clock = new DeviceClock();

        fromActivity = getIntent().getBooleanExtra("fromTeam", false);

        SharedPreferences spf = getSharedPreferences("HEIGHT", MODE_PRIVATE);
        int feet = spf.getInt("FEET", 0);
        int inches = spf.getInt("INCHES", 0);
        int totalHeight = inches + ( HEIGHT_FACTOR * feet );
        strideLength = totalHeight * STRIDE_CONVERSION;

        SharedPreferences emailPref = getSharedPreferences("USER_ID", MODE_PRIVATE);
        userEmail = emailPref.getString("EMAIL_ID", null);

        final Button pauseButton = findViewById(R.id.btn_pause_walk);
        final Button continueButton = findViewById(R.id.btn_continue_walk);
        final Button stopButton = findViewById(R.id.btn_stop_walk);

        stopwatchText = findViewById(R.id.text_time_value);
        stepsText = findViewById(R.id.text_steps_value);
        distanceText = findViewById(R.id.text_distance_value);

        /* starts the stopwatch */
        if (runner != null) {
            runner.cancel(true);
        }

        runner = new AsyncTaskRunner();
        runner.execute();

        continueButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);

        if ( getIntent().hasExtra("ROUTE_TITLE")) {
            routeTitle = getIntent().getExtras().getString("ROUTE_TITLE");
            TextView routeName = findViewById(R.id.text_route_name);
            routeName.setText(routeTitle);
            Log.d(TAG, "onCreate: routeTitle: " + routeTitle);
        }

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeWhenPaused += timeElapsed;
                if (runner != null) {
                    runner.cancel(true);
                }

                continueButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (runner != null) {
                    runner.cancel(true);
                }

                runner = new AsyncTaskRunner();
                runner.execute();

                continueButton.setVisibility(View.GONE);
                stopButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
            }
        });

        stopButton.setOnClickListener((view) -> {
            if ( !fromActivity ) {
                launchRouteInfoPage();
            } else {
                insertWalk(getIntent());
            }
        });
    }

    private void launchRouteInfoPage() {
        Log.d(TAG, "launchRouteInfoPage: launching the route information page");
        Intent intent = new Intent(this, RouteInfoActivity.class);
        intent.putExtra("ROUTE_TITLE", routeTitle);
        intent.putExtra("duration", stopwatchText.getText().toString());
        intent.putExtra("distance", distanceText.getText().toString());
        startActivityForResult(intent, LAUNCH_SECOND_ACTIVITY);
    }

    protected void setClock(IClock clock) {
        this.clock = clock;
    }

    protected int getNumSteps() {
        temporaryNumSteps += 10;
        return temporaryNumSteps;
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        private int startTime;

        @Override
        protected void onPreExecute() {
            startTime = clock.getCurrentClock();
        }

        @Override
        protected String doInBackground(String... params) {
            while (true){
                timeElapsed = clock.getCurrentClock() - startTime;
                int updateTime = timeWhenPaused + timeElapsed;

                int hours = (updateTime / 3600)  % 60;
                int minutes = (updateTime / 60)  % 60;
                int seconds = updateTime % 60;

                String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                int numSteps = getNumSteps();
                String stepsString = String.format("%d", numSteps);

                double distance = (strideLength / MILE_FACTOR) * numSteps;
                String distanceString = String.format("%.2f mi", distance);

                publishProgress(timeString, stepsString, distanceString);

                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    return e.toString();
                }

                if (isCancelled()) {
                    break;
                }
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(String... text) {
            stopwatchText.setText(text[0]);
            stepsText.setText(text[1]);
            distanceText.setText(text[2]);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                insertWalk(data);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            }
        }
    }

    private void insertWalk(Intent data) {
        WalkDao walkDao = DaoFactory.getWalkDao();
        String routeName = routeTitle;
        if (routeName == null) {
            routeName = data.getExtras().getString("routeTitle");
        }
        Walk newEntry = new Walk();
        newEntry.time = System.currentTimeMillis();
        newEntry.duration = stopwatchText.getText().toString();
        newEntry.steps = stepsText.getText().toString();
        newEntry.distance = distanceText.getText().toString();
        newEntry.routeName = routeName;
//        if ( !fromActivity ) {
            newEntry.userID = userEmail;

        walkDao.insertAll(newEntry);
        finish();
    }
}
