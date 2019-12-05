package Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.visualphysics.ChapterVideoScreenActivity;
import com.visualphysics.R;

import java.util.ArrayList;

/**
 * Created by admin on 5/23/2016.
 */
public class Tab3Adapter extends RecyclerView.Adapter<Tab3Adapter.Tab3ViewHolder>{

    ArrayList<String> strings;
    Context context;

    public Tab3Adapter(ArrayList<String> strings,Context context)
    {
        this.strings=strings;
        this.context=context;
    }
    @Override
    public Tab3ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab3frag, null);
        Tab3ViewHolder viewHolder = new  Tab3ViewHolder(itemview);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final Tab3ViewHolder holder, int position) {

        if(position==strings.size()-1)
        {
            holder.divider.setVisibility(View.GONE);
        }

        holder.mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ViewDialog viewDialog = new ViewDialog();
                viewDialog.showDownLoadDialog(context, "DownLoad in progress.... Want to cancel download?", holder);


//                    holder.mDownload.setImageDrawable(mcontext.getResources().getDrawable(R.drawable.download_2));
//                    animation(holder.mDownload);



            }
        });

        holder.mDownLoad2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewDialog viewDialog = new ViewDialog();
                viewDialog.showDeleteDialog(context, "Delete the download videos of this chapter?", holder);

            }
        });


        holder.mClickableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChapterVideoScreenActivity.class);
                context.startActivity(intent);
            }
        });
    }

    void animation(final View v) {
        Animation comeagain;
        comeagain = AnimationUtils.loadAnimation(context, R.anim.scale_profile);
        v.startAnimation(comeagain);
        comeagain.setDuration(200);
        comeagain.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return strings.size();
    }

    static class Tab3ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout mClickableLayout;
        ImageView mDownload,mDownLoad2;
        View divider;

        public Tab3ViewHolder(View itemView) {
            super(itemView);

            divider=(View)itemView.findViewById(R.id.divider_tab3);
            mClickableLayout=(LinearLayout)itemView.findViewById(R.id.tab3clickable_LinearLayout);

            mDownload=(ImageView)itemView.findViewById(R.id.tab3Fragdownload1);
            mDownLoad2 = (ImageView) itemView.findViewById(R.id.tab3Fragdownload2);

        }
    }



    class ViewDialog {
        public void showDownLoadDialog(Context activity, String msg, final  Tab3ViewHolder holder) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.custom_dialog);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
            text.setText(msg);

            Button no = (Button) dialog.findViewById(R.id.btn_no);
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    holder.mDownload.setImageDrawable(context.getResources().getDrawable(R.drawable.download_1));
                    dialog.dismiss();

                }


            });

            Button yes = (Button) dialog.findViewById(R.id.btn_yes);
            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    holder.mDownload.setVisibility(View.GONE);
                    holder.mDownLoad2.setVisibility(View.VISIBLE);
                    animation(holder.mDownLoad2);
                    dialog.dismiss();


                }
            });

            dialog.show();
            dialog.getWindow().setAttributes(lp);
        }


        public void showDeleteDialog(Context activity, String msg, final  Tab3ViewHolder holder) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.custom_dialog);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
            text.setText(msg);

            Button no = (Button) dialog.findViewById(R.id.btn_no);
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    holder.mDownLoad2.setImageDrawable(context.getResources().getDrawable(R.drawable.download_2));
                    dialog.dismiss();
                }


            });

            Button yes = (Button) dialog.findViewById(R.id.btn_yes);
            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    holder.mDownLoad2.setVisibility(View.GONE);
                    holder.mDownload.setVisibility(View.VISIBLE);
                    animation(holder.mDownload);
                    dialog.dismiss();



                }
            });

            dialog.show();
            dialog.getWindow().setAttributes(lp);
        }


    }
}
