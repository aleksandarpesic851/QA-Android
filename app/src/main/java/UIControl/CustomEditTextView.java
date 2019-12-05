package UIControl;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

/**
 * Created by India on 4/12/2016.
 */
public class CustomEditTextView extends AppCompatEditText {
    public CustomEditTextView(Context context) {
        super(context);
        init();
    }

    public CustomEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomEditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {

        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato_Regular.ttf");
        setTypeface(tf);
    }
}
