package deeplink;

import android.net.Uri;

/**
 * Created by iziss on 11/10/18.
 */
public interface OnLinkGenerateListener {

    public void onSuccess(String link);

    public void onFail(String error);
}
