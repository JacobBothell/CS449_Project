package jacobbothell.weatherboth;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                                               .setAction("Action", null).show();
                                   }
                               });
        */
        thingspeak(100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //creates the graphs
    //view is the item in the xml file
    //float is the data field being displayed
    //it is assumed that the data can be displayed in a linear manner with respect to the x-axis
    //a.k.a. the spacing is even
    void createGraph(int view, float[] data) {
        //creates the data points array passed to the graph
        DataPoint[] theStuff = new DataPoint[data.length];

        //extracts the points from the array and makes points out of them
        for (int i = 0; i < data.length; i++) {
            theStuff[i] = new DataPoint(i, data[i]);
        }

        //makes the graph view
        GraphView graph = (GraphView) findViewById(view);

        //puts all of the new points into the graph
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(theStuff);
        graph.addSeries(series);
    }

    //method gets the data
    //    updates the temperature data field and passes data to graph function
    void thingspeak(int dataPoints) {
        final TextView temp = (TextView) findViewById(R.id.temp);

        //instantiate the request queue
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "https://api.thingspeak.com/channels/172443/feed.json?key=PEA1BQZRVR62YHWK&results=";

        //Request a string response from url
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url + String.valueOf(dataPoints), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //getting the data out of the response
                try {
                    //gets arrays out of the json object
                    //this is data about the channel specifically
                    JSONObject jFields = response.getJSONObject("channel");
                    //this is the real data
                    JSONArray jData = response.getJSONArray("feeds");

                    //used to hold the data to put into graph
                    float[] humidity = new float[jData.length()];
                    float[] pressure = new float[jData.length()];
                    //used as a temporary item from the array
                    JSONObject tempo;

                    //iterates though the object
                    for (int i = 0; i < jData.length(); i++) {
                        tempo = jData.getJSONObject(i);
                        //puts the current temp data into the view
                        if (i == 0) {
                            temp.setText(tempo.getInt("field1") + " \u2103");
                        }
                        //saves the data in the array
                        //humidity data
                        humidity[i] = Float.parseFloat(tempo.getString("field3"));
                        //pressure data
                        pressure[i] = Float.parseFloat(tempo.getString("field2"));
                    }

                    //sends data to the graph creation function
                    createGraph(R.id.humidity, humidity);
                    createGraph(R.id.pressure, pressure);
                } catch (JSONException e) {
                    temp.setText("response error: " + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                temp.setText("No Response!");
            }
        });

        //access the requestqueue through your singleton class
        queue.add(jsObjRequest);
    }
}