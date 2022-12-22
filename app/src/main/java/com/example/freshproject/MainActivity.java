package com.example.freshproject;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.view.View.OnClickListener;
import android.os.AsyncTask;

import com.microsoft.maps.MapRenderMode;
import com.microsoft.maps.MapView;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.Geoposition;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.AltitudeReferenceSystem;
import com.microsoft.maps.MapFlyout;
import com.microsoft.maps.MapScene;
import com.microsoft.maps.MapAnimationKind;
import com.example.freshproject.model.SukiStore;

import java.io.InputStream;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.util.Scanner;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MapView mMapView;
    private Button btnLocateMe;
		@Override
		protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.activity_main);

		  mMapView = new MapView(this, MapRenderMode.VECTOR);  // or use MapRenderMode.RASTER for 2D map
		  mMapView.setCredentialsKey(BuildConfig.CREDENTIALS_KEY);
		  ((FrameLayout)findViewById(R.id.map_view)).addView(mMapView);
		  mMapView.onCreate(savedInstanceState);

		  btnLocateMe = (Button) findViewById(R.id.btnLocateMe);
		  btnLocateMe.setOnClickListener(this);
		}

		@Override
		public void onClick(View view){
			switch(view.getId()){
				case R.id.btnLocateMe:
					Geoposition currentLocation = findGPSLocation();
					mMapView.setScene(MapScene.createFromLocationAndRadius(new Geopoint(currentLocation), 200), MapAnimationKind.LINEAR);
					findNearestSukiStore();
					break;
				default:
					break;				
			}
		}
		
		public static Geoposition findGPSLocation(){
			// dummy data for the meantime because Anbox emulator don't have GPS
			return new Geoposition(14.558363435265706,121.08417311650506); 
		}

		public void findNearestSukiStore(){
			showSukiStore(new SukiStore("My Dummy Location",14.55836343526571,121.0841731165051,15));
			
			String strGPSLat = String.valueOf(findGPSLocation().getLatitude());
			String strGPSLon = String.valueOf(findGPSLocation().getLongitude());
			
			NearestStoreTask nearestStoreTask = new NearestStoreTask();
			nearestStoreTask.execute(strGPSLat, strGPSLon);
		}

		public void showSukiStore(SukiStore sukiStore){

			MapIcon icon = new MapIcon();
			icon.setLocation(new Geopoint(sukiStore.getLatitude(), sukiStore.getLongitude()));

			MapElementLayer elementLayer = new MapElementLayer();
			elementLayer.getElements().add(icon);

			mMapView.getLayers().add(elementLayer);
			MapFlyout flyout = new MapFlyout();
			flyout.setTitle(sukiStore.getStoreName().replace("[","").replace("]","").replace("\"",""));
			flyout.setDescription("[Detail]");
			icon.setFlyout(flyout);
		}

		@Override
		protected void onStart() {
			 super.onStart();
			 mMapView.onStart();
		}

		@Override
		protected void onResume() {
			 super.onResume();
			 mMapView.onResume();
		}

		@Override
		protected void onPause() {
			 super.onPause();
			 mMapView.onPause();
		}

		@Override
		protected void onSaveInstanceState(Bundle outState) {
			 super.onSaveInstanceState(outState);
			 mMapView.onSaveInstanceState(outState);
		}

		@Override
		protected void onStop() {
			 super.onStop();
			 mMapView.onStop();
		}

		@Override
		protected void onDestroy() {
			 super.onDestroy();
			 mMapView.onDestroy();
		}

		@Override
		public void onLowMemory() {
			 super.onLowMemory();
			 mMapView.onLowMemory();
		}

	private final class NearestStoreTask extends AsyncTask<String, String, String> { 

	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        // display a progress dialog to show the user what is happening
		}

		@Override
	    protected void onProgressUpdate(String... text) {
	            
	    }

	    @Override
	    protected String doInBackground(String... params) {
	    	String responseBody = "";
			String url = "https://nearestsukistore.azurewebsites.net/api/HttpExample/";
	    	try {

		    	String charset = "UTF-8";
				String paramLatitude = params[0];
				String paramLongitude = params[1];
				String query = String.format("name=%s",URLEncoder.encode(paramLatitude+","+paramLongitude, charset));
				URLConnection connection = new URL(url+"?"+query).openConnection();
				connection.setRequestProperty("Accept-Charset", charset);

				InputStream response = connection.getInputStream();
				try (Scanner scanner = new Scanner(response)) {
				    responseBody = scanner.useDelimiter("\\A").next();
				    System.out.println(responseBody + "hello");
				}

				if (responseBody.equals("") || responseBody.equals("[]")){
					return "No Nearby SukiStores";
				}

				// Deserializing JSON List<SukiStore>
				SukiStore[] arrSukiStores = (new Gson()).fromJson(responseBody, SukiStore[].class);
				responseBody = arrSukiStores[0].getStoreName();

				for(SukiStore sukiStore: arrSukiStores){
					showSukiStore(sukiStore);
				}

	    	} catch (Exception exc){
	    		responseBody = String.valueOf(exc);
	    	}
			return responseBody;
		}

	    @Override
	    protected void onPostExecute(String s) {

	    }
	}
}

//Toast.makeText(MainActivity.this, "Test test", Toast.LENGTH_SHORT).show();