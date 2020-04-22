package com.example.computerscience1;
// create onItemClickListener for admin menu
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button submit, btn_available;
    Spinner course_spinner, branch_spinner, year_spinner;
    EditText nameET, rollnoET, dobET;
    String name, rollno, dob, course, branch, year;
    boolean validation = false, alreadyEnrolled = false;
    String courses[] = {"Multimedia", "Linux shell programming", "Python programming", "Web designing", "Software testing"};
    ArrayList<String> AvailableCourses = new ArrayList<String>();// list of available courses
    int register_count[] = new int[courses.length]; // count of students already enrolled in the course
    int limit[] = {60, 60, 60, 60, 60};
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<String> enrolledRollNo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner_initialize();
        if (!(isConnected()))
            Toast.makeText(this, "Network connection unavailable", Toast.LENGTH_LONG).show();

        get_count();
        getAvailableCourse();

        submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(this);

        btn_available = (Button) findViewById(R.id.available);
        btn_available.setOnClickListener(this);

        //start
        rollnoET = (EditText) findViewById(R.id.rollno);
        rollnoET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {

                    rollno = rollnoET.getText().toString().toUpperCase();
                    if (rollno.equals(""))
                        Toast.makeText(MainActivity.this, "Rollno is empty", Toast.LENGTH_LONG).show();
                    else if (rollno.length() != 8)
                        Toast.makeText(MainActivity.this, "Invalid roll no", Toast.LENGTH_LONG).show();
                    else {
                        alreadyEnrolled = false;
                        for (int i = 0; i < courses.length; i++) {

                            db.collection(courses[i]).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot q) {

                                    for (DocumentSnapshot d : q.getDocuments()) {
                                        if (d.getId().equals(rollno))
                                            alreadyEnrolled = true;

                                    }
                                }
                            });
                        }

                    }
                }
            }
        });
    }
    //end

    @Override
    public void onClick(View view) {
        get_data_from_form();
        get_count();
        getAvailableCourse();

        switch (view.getId()) {
            case R.id.submit:
                validation = false;
                validate();
                if (validation) {
                    db.collection(course).document(rollno).set(new FormData(course, name, rollno, dob, branch, year)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    DocumentReference ref = db.collection("Count").document("count_data");
                    ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            try {
                                int count = Integer.parseInt(documentSnapshot.getString(course));
                                db.collection("Count").document("count_data").update(course, Integer.toString(count + 1));
                            } catch (NullPointerException e) {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    /*int index = indexOf(course);//index of selected course
                    register_count[index] += 1;
                    getAvailableCourse();
                    enrolledRollNo.add(rollno);
                    db.collection("Count").document("count_data").update(course,Integer.toString(register_count[index]+1));*/
                }
                break;
            case R.id.available:
                String s = "Available Courses:\n";
                for (int i = 0; i < AvailableCourses.size(); i++)
                    s += "*" + AvailableCourses.get(i) + "\n";
                Toast.makeText(this, s, Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void spinner_initialize() {

        branch_spinner = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> branch = ArrayAdapter.createFromResource(this, R.array.branch, R.layout.support_simple_spinner_dropdown_item);
        branch.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        branch_spinner.setAdapter(branch);

        year_spinner = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> year = ArrayAdapter.createFromResource(this, R.array.year, R.layout.support_simple_spinner_dropdown_item);
        year.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        year_spinner.setAdapter(year);

        course_spinner = (Spinner) findViewById(R.id.spinner3);
        ArrayAdapter<CharSequence> course = ArrayAdapter.createFromResource(this, R.array.course, R.layout.support_simple_spinner_dropdown_item);
        course.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        course_spinner.setAdapter(course);
    }

    public void get_data_from_form() {

        nameET = (EditText) findViewById(R.id.name);
        name = nameET.getText().toString();

        dobET = (EditText) findViewById(R.id.dob);
        dob = dobET.getText().toString();

        course = course_spinner.getSelectedItem().toString();
        branch = branch_spinner.getSelectedItem().toString();
        year = year_spinner.getSelectedItem().toString();
    }

    public void validate() {

        if (!isConnected())
            Toast.makeText(this, "Network connection unavailable", Toast.LENGTH_LONG).show();
        else if (AvailableCourses.isEmpty())
            Toast.makeText(this, "All the courses are registered", Toast.LENGTH_LONG).show();
        else if (course.equalsIgnoreCase("Select course")) {

            Toast.makeText(this, "Select a course", Toast.LENGTH_SHORT).show();
        } else if (!(AvailableCourses.contains(course))) {
            Toast.makeText(this, "Registration is full for this course", Toast.LENGTH_SHORT).show();
        } else if (name.isEmpty()) {

            Toast.makeText(this, "Name is empty", Toast.LENGTH_SHORT).show();
        } else if (rollno.isEmpty()) {

            Toast.makeText(this, "Roll number is empty", Toast.LENGTH_SHORT).show();
        } else if (rollno.length() != 8) {

            Toast.makeText(this, "Invalid roll number", Toast.LENGTH_SHORT).show();
        } else if (alreadyEnrolled) {
            Toast.makeText(this, "This roll no already registered in a course", Toast.LENGTH_SHORT).show();
        } else if (dob.isEmpty()) {

            Toast.makeText(this, "Dob is empty", Toast.LENGTH_SHORT).show();
        } else if (branch.equalsIgnoreCase("Select branch")) {

            Toast.makeText(this, "Select a branch", Toast.LENGTH_SHORT).show();
        } else if (year.equalsIgnoreCase("Select year")) {

            Toast.makeText(this, "Select a year", Toast.LENGTH_SHORT).show();
        } else
            validation = true;
    }

    public boolean isConnected() {
        try {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkinfo = null;
            if (manager != null) {
                networkinfo = manager.getActiveNetworkInfo();
            }
            return networkinfo != null && networkinfo.isConnected();
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void getAvailableCourse() {
        AvailableCourses.clear();
        for (int i = 0; i < courses.length; i++) {
            if (register_count[i] < limit[i])
                AvailableCourses.add(courses[i]);
        }
    }

    public int indexOf(String s) {

        for (int i = 0; i < courses.length; i++) {
            if (courses[i].equalsIgnoreCase(s))
                return i;
        }
        return 10;
    }

    public void get_count() {
        DocumentReference ref = db.collection("Count").document("count_data");
        ref.get().addOnSuccessListener(this, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot d) {
                for (int i = 0; i < courses.length; i++) {
                    register_count[i] = Integer.parseInt(d.getString(courses[i]));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.homepage_menu,menu);
        return true;
    }
}

