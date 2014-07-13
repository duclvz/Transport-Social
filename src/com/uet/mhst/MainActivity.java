package com.uet.mhst;

import java.io.IOException;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;

import net.duclv.myendpoint.Myendpoint;
import net.duclv.myendpoint.model.MyRequest;
import net.duclv.myendpoint.model.MyResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

public class MainActivity extends Activity {
	private Myendpoint endpoint;
	private GoogleAccountCredential credential;
	private SharedPreferences settings;
	private String accountName;
	static final int REQUEST_ACCOUNT_PICKER = 2;
	static final String WEB_CLIENT_ID = "169112209312-b414ngp468k01gl77ic23jh6v3l6cisp.apps.googleusercontent.com";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		settings = getSharedPreferences("Transport-Social", 0);
		credential = GoogleAccountCredential.usingAudience(this,
				"server:client_id:" + WEB_CLIENT_ID);
		setAccountName(settings.getString("ACCOUNT_NAME", null));

		if (credential.getSelectedAccountName() != null) {
			// Already signed in, begin app!
			Toast.makeText(getBaseContext(),
					"Logged in with : " + credential.getSelectedAccountName(),
					Toast.LENGTH_SHORT).show();
			// Toast.makeText(getBaseContext(),
			// GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext()),Toast.LENGTH_SHORT).show();
		} else {
			// Not signed in, show login window or request an account.
			chooseAccount();
		}

		Myendpoint.Builder endpointBuilder = new Myendpoint.Builder(
				AndroidHttp.newCompatibleTransport(), new JacksonFactory(),
				credential);
		endpoint = endpointBuilder.build();

		Button button = (Button) findViewById(R.id.button1);

		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new DoSomethingAsync(this, endpoint).execute();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_ACCOUNT_PICKER:
			if (data != null && data.getExtras() != null) {
				String accountName = data.getExtras().getString(
						AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					setAccountName(accountName);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("ACCOUNT_NAME", accountName);
					editor.commit();
					// User is authorized.
				}
			}
			break;
		}
	}

	private void chooseAccount() {
		startActivityForResult(credential.newChooseAccountIntent(),
				REQUEST_ACCOUNT_PICKER);
	}

	private class DoSomethingAsync extends AsyncTask<Void, Void, MyResult> {
		private Myendpoint endpoint;

		public DoSomethingAsync(OnClickListener onClickListener,
				Myendpoint endpoint) {
			this.endpoint = endpoint;
		}

		@Override
		protected MyResult doInBackground(Void... params) {
			try {
				MyRequest r = new MyRequest();
				r.setMessage("Tesla");
				return endpoint.compute(r).execute();
			} catch (IOException e) {
				e.printStackTrace();
				MyResult r = new MyResult();
				r.setValue("EXCEPTION");
				return r;
			}
		}

		@Override
		protected void onPostExecute(MyResult r) {
			TextView t = (TextView) findViewById(R.id.textView1);
			t.setText(r.getValue());
		}
	}

	private void setAccountName(String accountName) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("ACCOUNT_NAME", accountName);
		editor.commit();
		credential.setSelectedAccountName(accountName);
		this.accountName = accountName;
	}
}