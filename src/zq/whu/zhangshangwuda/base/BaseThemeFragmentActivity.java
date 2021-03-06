package zq.whu.zhangshangwuda.base;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import zq.whu.zhangshangwuda.ui.MyApplication;
import zq.whu.zhangshangwuda.ui.R;
import android.content.Intent;
import android.os.Bundle;

public class BaseThemeFragmentActivity extends UmengSherlockFragmentActivity {
	public int mTheme = R.style.MyLightTheme;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			mTheme = PreferenceHelper.getTheme(this);
		} else {
			mTheme = savedInstanceState.getInt("theme");
		}
		setTheme(mTheme);
		MyApplication.setActivity(this);
		MyApplication.setLayoutInflater(null);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mTheme != PreferenceHelper.getTheme(this)) {
			reload();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("theme", mTheme);
	}

	protected void reload() {
		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
	}
}
