package cse110.ucsd.team12wwr.firebase;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import cse110.ucsd.team12wwr.MainActivity;

public class FirebaseScheduleDao {
    public static final String SCHEDULE_COLLECTION_KEY = "schedules";
    FirebaseFirestore db;

    public FirebaseScheduleDao() {
        if (!MainActivity.unitTestFlag) {
            db = FirebaseFirestore.getInstance();
        }
    }

    public void insertAll(Schedule... schedules) {
        if (db == null) {
            return;
        }

        for (Schedule schedule : schedules) {
            db.collection(SCHEDULE_COLLECTION_KEY).document(schedule.teamID).set(schedule);
        }
    }

    public void delete(String teamID) {
        if (db == null) {
            return;
        }

        db.collection(SCHEDULE_COLLECTION_KEY).document(teamID).delete();
    }

    //    @Query("SELECT * FROM Schedule s WHERE s.teamID=:teamID")
    public void findScheduleByTeam(String teamID, OnCompleteListener<QuerySnapshot> listener) {
        if (db == null) {
            return;
        }

        db.collection(SCHEDULE_COLLECTION_KEY).whereEqualTo("teamID", teamID).get()
                .addOnCompleteListener(listener);
    }
}
