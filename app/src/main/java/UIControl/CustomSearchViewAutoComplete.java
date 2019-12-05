package UIControl;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class CustomSearchViewAutoComplete extends SearchView {

    private AutoCompleteTextView mSearchAutoComplete;

    public CustomSearchViewAutoComplete(Context context) {
        super(context);
        initialize();
    }

    public CustomSearchViewAutoComplete(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public void initialize() {

        mSearchAutoComplete = this.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato_Regular.ttf");
        mSearchAutoComplete.setTypeface(tf);
        setAutoCompleSuggestionsAdapter(null);
        setOnItemClickListener(null);
    }

    @Override
    public void setSuggestionsAdapter(CursorAdapter adapter) {
        throw new UnsupportedOperationException("Please use setAutoCompleSuggestionsAdapter(ArrayAdapter<?> adapter) instead");
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mSearchAutoComplete.setOnItemClickListener(listener);
    }

    public void setAutoCompleSuggestionsAdapter(ArrayAdapter<?> adapter) {
        mSearchAutoComplete.setAdapter(adapter);
    }

    public void setText(String text) {
        mSearchAutoComplete.setText(text);
    }
}
