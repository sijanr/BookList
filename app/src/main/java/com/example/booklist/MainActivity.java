package com.example.booklist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String urlAPI = "https://www.googleapis.com/books/v1/volumes?q=";

    private TextView text;

    private String searchParameter;

    private ProgressBar progressBar;

    private TextView finalText;

    private RecyclerView recyclerView;

    private BookListAdapter bookListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SearchView searchView = (SearchView) findViewById(R.id.searchView);


        text = (TextView) findViewById(R.id.searching_text_view);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        recyclerView = findViewById(R.id.recycler_view);

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

    private class BookListAsyncTask extends AsyncTask<String, Void, List<BookInfo>> {

        @Override
        protected void onPreExecute() {
            text.setVisibility(View.VISIBLE);
            text.setText("Searching for " + searchParameter);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<BookInfo> bookList) {

            text.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            bookListAdapter = new BookListAdapter(MainActivity.this, bookList);
            recyclerView.setAdapter(bookListAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        }


        @Override
        protected List<BookInfo> doInBackground(String... strings) {
            if (strings.length < 1) {
                return null;
            }

            // create a string url by adding user search keyword to the api url
            String urlCreate = urlAPI + strings[0] + "&maxResults=6";

            // create a URL to make a connection to
            URL createdURL = createURL(urlCreate);

            //get the json response from the url
            String jsonResponse = makeHTTPRequest(createdURL);

            return parseJSON(jsonResponse);

        }
    }


    /**
     * Create  a URL from a string
     */
    private URL createURL(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException exception) {
            Log.e("MainActivity.java", "Failed to create URL");
        }
        return url;
    }


    /**
     * Make a HTTP request and return the string from the request
     */
    private String makeHTTPRequest(URL url) {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();

            //if the url connection was successful then read the stream and pass the response
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }

        } catch (IOException exception) {
            Log.e("MainActivity.java", "Failed to retrieve JSON results");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException exception) {
                    Log.e("MainActivity.java", "InputStream error" + exception);
                }
            }
        }

        return jsonResponse;
    }


    /**
     * Convert input stream to String which contains the JSON response from the server
     */
    private String readFromStream(InputStream inputStream) {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            try {
                String line = bufferedReader.readLine();
                while (line != null) {
                    output.append(line);
                    line = bufferedReader.readLine();
                }
            } catch (IOException exception) {
                Log.e("MainActivity.java", "Error reading from the input stream" + exception);
            }
        }

        return output.toString();
    }


    /**
     * Parse the JSON string to get the book name and the author
     */
    private List<BookInfo> parseJSON(String jsonString) {

        List<BookInfo> bookInfo = new ArrayList<>();

        if (jsonString != null && jsonString.length() > 0) {
            try {
                JSONObject rootObject = new JSONObject(jsonString);

                //get the array of the information of the items containing the keyword phrase
                JSONArray itemArray = rootObject.getJSONArray("items");

                for (int i = 0; i < itemArray.length(); i++) {
                    //get the  item from the array
                    JSONObject currentItem = itemArray.getJSONObject(i);

                    //get the author and book name of the current item
                    JSONObject volumeInfo = currentItem.getJSONObject("volumeInfo");
                    String bookName = volumeInfo.getString("title");
                    JSONArray authorsArray = volumeInfo.optJSONArray("authors");
                    String authors = "";
                    if (authorsArray != null) {
                        for (int j = 0; j < authorsArray.length(); j++) {
                            if (j < authorsArray.length() - 1) {
                                authors += authorsArray.get(j) + ", ";
                            } else {
                                authors += authorsArray.get(j);
                            }

                        }
                    }

                    //get the publication name, published date and rating of the book
                    String publication = volumeInfo.optString("publisher");
                    String publishedDate = volumeInfo.optString("publishedDate").substring(0, 4);
                    int pageCount = volumeInfo.optInt("pageCount");

                    JSONObject imageObject = volumeInfo.optJSONObject("imageLinks");
                    String imageURL = "";
                    if (imageObject != null) {
                        imageURL = imageObject.optString("smallThumbnail");
                    } else {
                        imageURL = getResources().getString(R.string.no_image_url);
                    }
                    float rating = (float) volumeInfo.optDouble("averageRating");

                    //add the book to the list
                    bookInfo.add(new BookInfo(bookName, authors, imageURL, rating, publication, publishedDate, pageCount));
                }


            } catch (JSONException exception) {
                Log.e("MainActivity.java", "Failed to parse JSON. \n" + exception);
            }
        }

        return bookInfo;

    }
}
