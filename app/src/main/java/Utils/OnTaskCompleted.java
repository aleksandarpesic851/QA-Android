package Utils;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/***
 * This Interface is used for the Callback listner for the API call
 */
public class OnTaskCompleted {

    public interface CallBackListener {

        void onTaskCompleted(JSONObject result);

        void onTaskCompleted(JSONObject result, String Method);

        void onTaskCompleted(String result, String Method);

        void onError(VolleyError error,String Method);
    }

    CallBackListener listener;

    public OnTaskCompleted(CallBackListener listener) {
        this.listener = listener;
    }

    public void onTaskCompleted(JSONObject result) {
        listener.onTaskCompleted(result);
    }

    public void onTaskCompleted(JSONObject result, String Method) {
        listener.onTaskCompleted(result, Method);
    }

    public void onTaskCompleted(String result, String Method) {
        listener.onTaskCompleted(result, Method);
    }

    public void onError(VolleyError error,String Method) {
        listener.onError(error,Method);
    }
}
