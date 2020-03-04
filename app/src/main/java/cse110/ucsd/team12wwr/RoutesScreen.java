package cse110.ucsd.team12wwr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import cse110.ucsd.team12wwr.firebase.FirebaseRouteDao;
import cse110.ucsd.team12wwr.firebase.Route;

public class RoutesScreen extends AppCompatActivity {

    private static final String TAG = "RoutesScreen";

    private String routeName;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_screen);

        Button back = findViewById(R.id.back_button);
        Button add = findViewById(R.id.add_button);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchRouteInfoActivity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listView = findViewById(R.id.list_view);

        FirebaseRouteDao routeDao = new FirebaseRouteDao();
        routeDao.retrieveAllRoutes().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Route> routeList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    routeList.add(document.toObject(Route.class));
                }

                ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, routeList);
                listView.setAdapter(arrayAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        routeName = routeList.get(position).name;
                        launchRoutesDetailsPage();
                    }
                });
            }
        });
    }

    public void launchRoutesDetailsPage() {
        Intent intent = new Intent(this, RouteDetailsPage.class);
        intent.putExtra("name", routeName);
        startActivity(intent);
    }

    public void launchRouteInfoActivity() {
        Log.d(TAG, "launchRouteInfoActivity: launching the route information page");
        Intent intent = new Intent(this, RouteInfoActivity.class);

        startActivity(intent);
    }
}