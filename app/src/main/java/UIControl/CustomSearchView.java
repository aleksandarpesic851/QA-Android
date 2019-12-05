package UIControl;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by India on 7/27/2016.
 */
public class CustomSearchView extends SearchView{

    public CustomSearchView(Context context) {
        super(context);
        init();
    }

    public CustomSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        TextView searchText = (TextView)
                this.findViewById(android.support.v7.appcompat.R.id.search_src_text);

        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato_Regular.ttf");
        searchText.setTypeface(tf);
    }
}
