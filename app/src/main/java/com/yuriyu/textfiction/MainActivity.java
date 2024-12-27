package com.yuriyu.textfiction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.FragmentActivity;
import androidx.documentfile.provider.DocumentFile;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;


/**
 * From this activity, the player can manage his/her library and start games.
 * 
 * @author
 * 
 */
public class MainActivity extends FragmentActivity {

    private static final int RQS_OPEN_DOCUMENT_TREE = 2;
	LibraryFragment frag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		try {
			Field field = R.style.class.getField(prefs.getString("theme", ""));
			setTheme(field.getInt(null));
		}
		catch (Exception e) {
			Log.w(getClass().getName(), e);
		}

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_main);
//		AppRater.appLaunched(this);
		Uri game = getIntent().getData();
		if (game != null && game.getScheme().equals(ContentResolver.SCHEME_FILE)) {
			frag = (LibraryFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_library);
			File[] f = { new File(game.getPath()) };
			ImportTask.importGames(frag, f);
		}

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK && requestCode == RQS_OPEN_DOCUMENT_TREE){
			Uri uriTree = data.getData();
            assert uriTree != null;
			DocumentFile documentFile = DocumentFile.fromTreeUri(this, uriTree);
            assert documentFile != null;
			String fileName = "";
            for (DocumentFile file : documentFile.listFiles()) {
				DocumentFile directory = DocumentFile.fromTreeUri(this, file.getUri());
				if (!file.isDirectory()) {
					Cursor cursor = this.getContentResolver().query(file.getUri(), null, null, null, null);
                    assert cursor != null;
                    int columnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
					if (cursor.moveToFirst()) {
						fileName = cursor.getString(columnIndex);
					}
					cursor.close();
					File appExternalDir = new File(this.getExternalFilesDir(null), "/TextFiction/games/" + fileName);

                    try (InputStream in = getContentResolver().openInputStream(file.getUri())) {
						try (FileOutputStream out = new FileOutputStream(appExternalDir)) {
                            byte[] buf = new byte[1024];
							int len;
							while ((len = in.read(buf)) > 0) {
								out.write(buf, 0, len);
							}
						}
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }//Files.copy(src, appExternalDir.toPath());//API 26
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.mi_settings: {
				startActivity(new Intent(this, SettingActivity.class));
				return true;
			}
			case R.id.mi_access: {
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE );//_TREE
				startActivityForResult(intent, RQS_OPEN_DOCUMENT_TREE );
				return true;
			}
//			case R.id.mi_help: {
//				openUri(this,Uri.parse(getString(R.string.url_help)));
//			}
		}
		return false;
	}

	/**
	 * Open an url in a webbrowser
	 * 
	 * @param ctx
	 *          a context
	 * @param uri
	 *          target
	 */
	public static void openUri(Context ctx, Uri uri) {
		try {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
			ctx.startActivity(browserIntent);
		}
		catch (ActivityNotFoundException e) {
			// There are actually people who don't have a webbrowser installed
			Toast.makeText(ctx, R.string.msg_no_webbrowser, Toast.LENGTH_SHORT)
					.show();
		}
	}

}
