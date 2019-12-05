package Utils;


import android.util.Log;

public class DebugLog implements AppConstansts{


	public static void e(String tag, String msg) {
		if (IS_DEBUG) {
			Log.e(tag, msg);
		}
	}

	public static void v(String tag, String msg) {
		if (IS_DEBUG) {
			Log.v(tag,msg);
		}
	}

	public static void d(String TAG, String msg) {
		if (IS_DEBUG) {
			Log.d(TAG, msg);

		}
	}
}
