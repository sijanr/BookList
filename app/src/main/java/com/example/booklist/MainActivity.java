package com.example.booklist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    private final String urlAPI = "https://www.googleapis.com/books/v1/volumes?q=";

    private TextView text;

    private String searchParameter;

    private ProgressBar progressBar;

    private TextView finalText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SearchView searchView = (SearchView) findViewById(R.id.searchView);

        finalText = (TextView) findViewById(R.id.search_result_text_view);

        text = (TextView) findViewById(R.id.searching_text_view);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchParameter = searchView.getQuery().toString();

                BookListAsyncTask bookListAsyncTask = new BookListAsyncTask();
                bookListAsyncTask.execute(searchParameter);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

    }

    private class BookListAsyncTask extends AsyncTask<String,Void,String>{

        @Override
        protected void onPreExecute() {
            text.setVisibility(View.VISIBLE);
            text.setText("Searching for " + searchParameter);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {

            text.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            finalText.setText(s);

        }


        @Override
        protected String doInBackground(String... strings) {
            if(strings.length<1){
                return "No query found to search";
            }

            // create a string url by adding user search keyword to the api url
            String urlCreate = urlAPI + strings[0];

            // create a URL to make a connection to
            URL createdURL = createURL(urlCreate);

            //get the json response from the url
            String jsonResponse = makeHTTPRequest(createdURL);

            return parseJSON(jsonResponse);

        }
    }


    /**
     * Create  a URL from a string
     * */
    private URL createURL(String urlString){
        URL url = null;
        try{
            url = new URL(urlString);
        } catch(MalformedURLException exception){
            Log.e("MainActivity.java","Failed to create URL");
        }
        return url;
    }


    /**
     * Make a HTTP request and return the string from the request
     */
    private String makeHTTPRequest(URL url){
        String jsonResponse = "";

        if(url==null){
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try{
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();

            //if the url connection was successful then read the stream and pass the response
            if(urlConnection.getResponseCode()==200){
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }

        } catch(IOException exception){
            Log.e("MainActivity.java","Failed to retrieve JSON results");
        } finally {
            if(urlConnection!=null){
                urlConnection.disconnect();
            }
            if(inputStream!=null){
                try{
                    inputStream.close();
                } catch(IOException exception){
                    Log.e("MainActivity.java", "InputStream error" + exception);
                }
            }
        }

        return jsonResponse;
    }


    /**
     * Convert input stream to String which contains the JSON response from the server
     */
    private String readFromStream(InputStream inputStream){
        StringBuilder output = new StringBuilder();
        if(inputStream!=null){
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            try{
                String line = bufferedReader.readLine();
                while(line!=null){
                    output.append(line);
                    line = bufferedReader.readLine();
                }
            } catch(IOException exception){
                Log.e("MainActivity.java","Error reading from the input stream" + exception);
            }
        }

        return output.toString();
    }


    /**
     * Parse the JSON string to get the book name and the author
     * */
    private String parseJSON(String jsonString){

        String finalResult = "";

        if(jsonString!=null && jsonString.length()>0){
            try{
                JSONObject rootObject = new JSONObject(jsonString);

                //get the array of the information of the items containing the keyword phrase
                JSONArray itemArray = rootObject.getJSONArray("items");

                //get the first item from the array
                JSONObject firstResult = itemArray.getJSONObject(0);

                //get the publishers and book name of the first index item
                JSONObject firstItem = firstResult.getJSONObject("volumeInfo");
                String bookName = firstItem.getString("title");
                JSONArray authorsArray = firstItem.getJSONArray("authors");
                String authors = "";
                for(int i=0; i<authorsArray.length(); i++){
                    if(i<authorsArray.length()-1){
                        authors += authorsArray.get(i) + ", ";
                    }
                    else{
                        authors += authorsArray.get(i);
                    }

                }

                finalResult = bookName + " by " + authors;

            } catch(JSONException exception){
                Log.e("MainActivity.java", "Failed to parse JSON. \n" + exception);
            }
        }

        return finalResult;

    }
}
