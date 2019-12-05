package UIControl;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by India on 4/4/2016.
 */
public class CustomTextViewForRupeeSymbol extends TextView{
    public CustomTextViewForRupeeSymbol(Context context) {
        super(context);
        init();
    }

    public CustomTextViewForRupeeSymbol(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTextViewForRupeeSymbol(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato_Regular.ttf");
        setTypeface(tf);
    }
}
