package io.github.ymfyp2013.CommentARio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class CaptActivity extends Activity implements OnClickListener{
	private static final int CAMERA_REQUEST = 1919;
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	private static final String url = "http://gateway-ymfyp2013.rhcloud.com/targets/post";
	private ImageView captImgView;
	private Bitmap captImgBitmap;
	public File storageDir;
	public File lastImage;
	private boolean firstTime = true;
	private String targetTitle = null;
	private Button mCancelButton;
	private Button mOkButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capt);
		// Show the Up button in the action bar.
		//setupActionBar();
		
		mOkButton = (Button) findViewById(R.id.btn_ok);
		mOkButton.setOnClickListener(this);
        
        mCancelButton = (Button) findViewById(R.id.btn_cancel);
        mCancelButton.setOnClickListener(this);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.capt, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {  
		if(this.firstTime)
		{
		    // TODO Auto-generated method stub  
			this.firstTime = false;
			this.captImgView = (ImageView) findViewById(R.id.capt_view);
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			File f;
			try {
				f = createImageFile();
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				startActivityForResult(cameraIntent, CAMERA_REQUEST);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	    super.onWindowFocusChanged(hasFocus);  
	}
	
	private String getAlbumName()
	{
		return "commentARioAlbum";
	}
	
	@SuppressLint("SimpleDateFormat")
	protected File createImageFile() throws IOException{
		String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
		this.storageDir = new File(
			Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES
			),
			getAlbumName()
		);
		
		storageDir.mkdir();
		
		File image = File.createTempFile(timeStamp, JPEG_FILE_SUFFIX, this.storageDir);
		image.getAbsolutePath();
		
		this.lastImage = image;
		
		return image;
	}
	
	private void galleryAddPic(){
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(this.lastImage.getAbsolutePath());
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
	}
	
	private void setPic(){
		int targetW = this.captImgView.getWidth();
		int targetH = this.captImgView.getHeight();
		
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(lastImage.getAbsolutePath(), bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		
		int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
		
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;
		
		this.captImgBitmap = BitmapFactory.decodeFile(lastImage.getAbsolutePath(), bmOptions);
		
		this.captImgView.setImageBitmap(this.captImgBitmap);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
			this.galleryAddPic();
			this.setPic();
		}
	}
	
	public void onUploadPhoto() {
		EditText titleBox = (EditText) findViewById(R.id.title_input);
		this.targetTitle = titleBox.getText().toString();
		if(this.targetTitle.length() == 0)
		{
			showDialog("Sorry","Please enter a title to proceed");
			return;
		}
		try {
			postTarget();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onCancel() {
		finish();
	}
	
	private void postTarget() throws URISyntaxException, ClientProtocolException, IOException, JSONException {
		HttpPost postRequest = new HttpPost();
		postRequest.setURI(new URI(url));
		//JSONObject requestBody = new JSONObject();
		List<NameValuePair> requestBody = new ArrayList<NameValuePair>(2);
		
		setRequestBody(requestBody);
		postRequest.setEntity(new UrlEncodedFormEntity(requestBody));
		setHeaders(postRequest); // Must be done after setting the body
		
		new PostTask().execute(postRequest);
	}
	
	private byte[] getBytesFromBitmap(Bitmap bitmap){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 70, stream);
		return stream.toByteArray();
	}
	
	/*private void setRequestBody(JSONObject requestBody) throws IOException, JSONException {
		byte[] image = getBytesFromBitmap(this.captImgBitmap);
		requestBody.put("title", this.targetTitle); // Mandatory
		requestBody.put("image", Base64.encodeToString(image, Base64.NO_WRAP)); // Mandatory
	}*/
	
	private void setRequestBody(List<NameValuePair> requestBody) throws IOException, JSONException {
		byte[] image = getBytesFromBitmap(this.captImgBitmap);
		requestBody.add(new BasicNameValuePair("title", this.targetTitle)); // Mandatory
		requestBody.add(new BasicNameValuePair("image", Base64.encodeToString(image, Base64.NO_WRAP))); // Mandatory
	}
	
	private void setHeaders(HttpUriRequest request) {
		request.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
		request.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
	}
	
	public void showDialog(String title, String message) {
		new AlertDialog.Builder(this)
		.setTitle(title)
		.setMessage("Please enter a title to proceed")
		.setPositiveButton("OK",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		})
		.show();
	}
	
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId())
        {
        case R.id.btn_ok:
        	onUploadPhoto();
            break;
        case R.id.btn_cancel:
        	onCancel();
            break;
        }
	}
	
	private class PostTask extends AsyncTask<HttpPost, Void, HttpResponse> {
		private HttpClient client = new DefaultHttpClient();
		
		private ProgressDialog dialog = new ProgressDialog(CaptActivity.this);
		
		protected void onPreExecute(){
			dialog.setMessage("Uploading...");
			dialog.show();
		}
		
	    protected HttpResponse doInBackground(HttpPost... postRequest) {
	    	int count = postRequest.length;
	    	HttpResponse response = null;
	        for (int i = 0; i < count; i++) {
	        	try {
					response = client.execute(postRequest[i]);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
			return response;
	    }

	    /*protected void onProgressUpdate(Integer progress) {
	    }*/

	    protected void onPostExecute(HttpResponse response) {
	    	String responseBody = null;
			try {
				responseBody = EntityUtils.toString(response.getEntity());
				Log.v("response", responseBody);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	JSONObject jobj = null;
			try {
				jobj = new JSONObject(responseBody);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	Boolean success = null;
			try {
				success = jobj.has("result") ? jobj.getBoolean("result") : false;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			dialog.dismiss();
			
			if(success==null || !success)
			{
				Toast.makeText(getApplicationContext(), "Error: Unable to upload for now", Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Success: Your topic has been uploaded!", Toast.LENGTH_SHORT).show();
			}
	    }
	}
}