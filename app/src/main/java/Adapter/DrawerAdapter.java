package Adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.visualphysics.R;

import UIControl.CustomTextViewRegular;
import Utils.AppConstansts;
import Utils.AppUtil;

public class DrawerAdapter extends BaseAdapter {

    private String[] item_name;
    private int[] item_image;
    private Context context;
    private LayoutInflater inflater;

    /*int[] listItemBackground = new int[]{R.drawable.drawer_list_background,
            R.drawable.drawer_list_background};*/

    public DrawerAdapter(Context context) {

        this.context = context;
        item_name = AppConstansts.DRAWER_TITLE;
        item_image = AppConstansts.DRAWER_ITEM;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {

        return item_name.length;
    }

    @Override
    public Object getItem(int arg0) {

        return null;
    }

    @Override
    public long getItemId(int arg0) {

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup root) {

        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_drawer, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.txt_drawer_item.setText(item_name[position]);


        // int listItemBackgroundPosition = position % listItemBackground.length;
        //convertView.setBackgroundResource(listItemBackground[listItemBackgroundPosition]);


        if (position == AppUtil.SELECTED_POSITION) {

            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorNavigationGrey));
            viewHolder.iv_drawer_item.setImageResource(AppConstansts.DRAWER_ITEM_SELECTED[position]);


        } else {

            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
            viewHolder.iv_drawer_item.setImageResource(item_image[position]);

        }

        return convertView;
    }

    private class ViewHolder {
        CustomTextViewRegular txt_drawer_item;
        ImageView iv_drawer_item;

        public ViewHolder(View item) {
            txt_drawer_item = item.findViewById(R.id.txt_drawer_item);
            iv_drawer_item = item.findViewById(R.id.iv_drawer_item);
        }

    }

    public void setList() {

        item_name = AppConstansts.DRAWER_TITLE;

        notifyDataSetChanged();
    }

}
