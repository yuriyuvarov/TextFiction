package com.yuriyu.textfiction;
//TODO: make it work for A11+
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.List;

public class MyViewModel extends ViewModel {
	private File [] files = new File[2];
	private MutableLiveData<List<Uri>> myFile = new MutableLiveData<>();
	private MutableLiveData<String> myData = new MutableLiveData<>("initial value");
	public void setData(String data) {
		myData.setValue(data);
	}
	public LiveData<String> getData() {
		return myData;
	}

	public void setData(List<Uri> uri) {
		myFile.setValue(uri);
		files[0] = new File(uri.get(0).getPath());
//		files[0] = new File("/storage/emulated/0/TextFiction/anchor.z8");
//		files[1] = new File("/storage/emulated/0/Download/Text/LostPig.z8");
	}
	public LiveData<List<Uri>> getUri() {return myFile;}
	public File[] getFiles() {return files;}
}

//Activity onCreate
//myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
