package jacobbothell.weatherboth;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
//used for the http request
import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
//used for the graph data views
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

        //creates everything data-centric items
        thingspeak(100);

    }

    //creates the graphs
    //view is the item in the xml file
    //float is the data field being displayed
    //it is assumed that the data can be displayed in a linear manner with respect to the x-axis
            //a.k.a. the spacing is even
    void createGraph(int view, float[] data)
    {
        //creates the data points array passed to the graph
        DataPoint[] theStuff = new DataPoint[data.length];

        //extracts the points from the array and makes points out of them
        for(int i = 0; i < data.length; i++)
        {
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
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.thingspeak.com/channels/172443/feed.json?key=PEA1BQZRVR62YHWK&results=";

        //Request a string response from url
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url+String.valueOf(dataPoints), null, new Response.Listener<JSONObject>() {
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
                    for(int i=0; i < jData.length(); i++)
                    {
                        tempo = jData.getJSONObject(i);
                        //puts the current temp data into the view
                        if(i==0)
                        {
                            temp.setText(tempo.getInt("field1")+R.string.degree+"F");
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
                } catch(JSONException e) {
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