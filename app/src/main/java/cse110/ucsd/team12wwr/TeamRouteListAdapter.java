package cse110.ucsd.team12wwr;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import cse110.ucsd.team12wwr.firebase.FirebaseWalkDao;
import cse110.ucsd.team12wwr.firebase.Route;

import cse110.ucsd.team12wwr.ui.routes_tab.PersonalRoutesFragment;

public class TeamRouteListAdapter extends ArrayAdapter<Route> {

    private static final String TAG = "RouteListAdapter";
    private Context context;
    int resource;

    public TeamRouteListAdapter(Context context, int resource, ArrayList<Route> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String routeName = getItem(position).name;

        LayoutInflater inflater = LayoutInflater.from(this.context);
        convertView = inflater.inflate(this.resource, parent, false);

        TextView textViewRouteName = (TextView) convertView.findViewById(R.id.route_name);
        TextView textViewPrevWalked = (TextView) convertView.findViewById(R.id.previously_walked);

        textViewRouteName.setText(routeName);
        Log.d(TAG, "getView: Setting list item name");

        FirebaseWalkDao dao = new FirebaseWalkDao();
        dao.findByRouteName(routeName).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean hasWalk = false;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    hasWalk = true;
                }

                if (!hasWalk) {
                    textViewPrevWalked.setVisibility(View.INVISIBLE);
                }

                Log.i(TAG, "getView: Walk for " + routeName + " has been walked on before: " + hasWalk);
            }
        });

        return convertView;
    }

}
