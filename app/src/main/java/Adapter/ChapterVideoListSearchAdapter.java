package Adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.visualphysics.ApiCall;
import com.visualphysics.ChapterVideoScreenActivity;
import com.visualphysics.R;
import com.visualphysics.VideoPlayerScreenActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import Database.DataBase;
import Model.Videos;
import Model.VideosWithChapter;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.LicenseListener;
import Utils.LicenseUtil;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;

public class ChapterVideoListSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnTaskCompleted.CallBackListener {
    private SparseArray<ArrayList<VideosWithChapter>> mVideoSparsedArray;
    private Context context;
    private ProgressDialog mProgressDialog;
    private ApiCall mApiCall;
    private DataBase db;
    private VideosWithChapter mSelectedChapter;

    public ChapterVideoListSearchAdapter(SparseArray<ArrayList<VideosWithChapter>> sparseArray, Context context) {
        this.mVideoSparsedArray = sparseArray;
        this.context = context;
        if (context != null)
            this.mProgressDialog = new ProgressDialog(context);
        this.mApiCall = new ApiCall();
        db = new DataBase(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_search_chapter_item, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemView.setLayoutParams(lp);
        return new SearchItemViewHolder(itemView); // view holder for normal items
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        SearchItemViewHolder holder = ((SearchItemViewHolder) viewHolder);

        ArrayList<VideosWithChapter> videosWithChapters = mVideoSparsedArray.valueAt(position);

        if (!videosWithChapters.isEmpty()) {
            VideosWithChapter videosWithChapter = videosWithChapters.get(0);
            holder.tvChapterName.setText(videosWithChapter.ChapterName);
            holder.llChapterSearchItem.setTag(videosWithChapter);
            holder.llChapterSearchItem.setOnClickListener(v -> {
                VideosWithChapter chapterDetail = (VideosWithChapter) v.getTag();
                moveForward(chapterDetail);
            });

            HorizontalSearchAdapter horizontalSearchVideoAdapter = new HorizontalSearchAdapter(videosWithChapters,
                    context);
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            holder.rvAppSearch.setLayoutManager(layoutManager);
            holder.rvAppSearch.setAdapter(horizontalSearchVideoAdapter);
        }
    }

    /***
     * @param chapter
     */
    private void moveForward(VideosWithChapter chapter) {

        if (db.isChapterVideoAvailable(chapter.ChapterID)) {
            Intent intent = new Intent(context, ChapterVideoScreenActivity.class);
            intent.putExtra("CategoryID", Integer.parseInt(chapter.CategoryID));
            intent.putExtra("ChapterName", chapter.ChapterName);
            intent.putExtra("ChapterID", chapter.ChapterID);
            context.startActivity(intent);
        } else {
            mSelectedChapter = chapter;
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();
            mApiCall.getChapterVideos(new AppUtil(context).getDeviceID(), chapter.ChapterID,
                    new OnTaskCompleted(ChapterVideoListSearchAdapter.this), mApiCall.GetChapterVideos);

        }

    }

    @Override
    public int getItemCount() {
        if (mVideoSparsedArray != null) {
            return mVideoSparsedArray.size();
        } else {
            return 0;
        }
    }

    @Override
    public void onTaskCompleted(JSONObject result) {

    }

    @Override
    public void onTaskCompleted(JSONObject result, String Method) {
        dismissDialog();
        if (Method.equals(mApiCall.GetChapterVideos)) {
            parseResponseForChapterVideoNumber(result);
        }
    }

    @Override
    public void onTaskCompleted(String result, String Method) {

    }

    @Override
    public void onError(VolleyError error, String Method) {
        dismissDialog();
        Activity activity = (Activity) context;
        AppUtil.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                context.getResources().getString(R.string.Error_Msg_Try_Later));
    }

    void parseResponseForChapterVideoNumber(JSONObject response) {
        Activity activity = (Activity) context;
        try {
            JSONObject mJsonObj = response.getJSONObject(mApiCall.GetChapterVideos);

            int ErrorCode = mJsonObj.getInt("Error");

            if (ErrorCode == 1) {
                AppUtil.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                        context.getResources().getString(R.string.Error_Msg_Try_Later));


            } else if (ErrorCode == 2)
                AppUtil.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                        mJsonObj.getString("Message"));
            else if (ErrorCode == 0) {
                JSONArray jsonArray = new JSONArray(mJsonObj.getString("data"));
                ArrayList<Videos> mVideoArrayList = Videos.fromJson(jsonArray);
                db.doAddChapterVideos(mVideoArrayList);

                Intent intent = new Intent(context, ChapterVideoScreenActivity.class);
                intent.putExtra("CategoryID", Integer.parseInt(mSelectedChapter.CategoryID));
                intent.putExtra("ChapterName", mSelectedChapter.ChapterName);
                intent.putExtra("ChapterID", mSelectedChapter.ChapterID);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            ErrorLog.SaveErrorLog(e);
            e.printStackTrace();
            AppUtil.displaySnackBarWithMessage(activity.findViewById(android.R.id.content),
                    context.getResources().getString(R.string.Error_Msg_Try_Later));
            ErrorLog.SendErrorReport(e);

        }
    }

    /***
     * Dismiss Dialog
     */
    private void dismissDialog() {
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.SaveErrorLog(e);
            ErrorLog.SendErrorReport(e);
        }
    }

    static class SearchItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvChapterName;
        private RecyclerView rvAppSearch;
        private LinearLayout llChapterSearchItem;

        SearchItemViewHolder(View itemView) {
            super(itemView);
            tvChapterName = itemView.findViewById(R.id.tvChapterName);
            rvAppSearch = itemView.findViewById(R.id.rvAppSearch);
            llChapterSearchItem = itemView.findViewById(R.id.llChapterSearchItem);
        }
    }

    private class HorizontalSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int REQUEST_PERMISSION_CODE = 101;
        private final ArrayList<VideosWithChapter> videosWithChapters;
        private LicenseListener OnVideoPlayLicenseListener;

        public HorizontalSearchAdapter(ArrayList<VideosWithChapter> videosWithChapters, Context context) {
            this.videosWithChapters = videosWithChapters;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_video_item_search, null);
            return new VideoViewHolder(itemView); // view holder for normal items
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
            VideoViewHolder holder = ((VideoViewHolder) viewHolder);
            if (position == 0) {
                holder.itemView.setPadding(26, 2, 2, 2);
            } else {
                holder.itemView.setPadding(2, 2, 2, 2);
            }

            VideosWithChapter videosWithChapter = videosWithChapters.get(position);
            holder.tvVideoTitle.setText(videosWithChapter.Title);
            holder.tvVideoDesc.setText(videosWithChapter.Description);
            holder.tvVideoDuration.setText(videosWithChapter.Duration + " mins");

            SharedPrefrences mSharedPref = new SharedPrefrences(context);

            Picasso.get()
                    .load(mSharedPref.getPreferences(mSharedPref.STREAM_URL, "") + videosWithChapter.Image)
                    .error(R.drawable.default_img)
                    .placeholder(R.drawable.default_img)
                    .into(holder.ivVideoThumbnail);
            holder.itemView.setTag(videosWithChapter);

            holder.itemView.setOnClickListener(v -> {
                VideosWithChapter mSelectedChapterInner = (VideosWithChapter) v.getTag();
                if (!LicenseUtil.isLicenseExpiry(context)) {
                    playVideoScreen(mSelectedChapterInner);
                } else {
                    AppUtil.displaySnackBarWithMessage(((Activity) context).findViewById(android.R.id.content),
                            context.getResources().getString(R.string.msg_license_expired));
                    setOnVideoPlayLicenseProcessListener();
                    LicenseUtil License = new LicenseUtil(this.OnVideoPlayLicenseListener);
                    License.acquireLicence(context, mSharedPref.getLoginUser().getStudentID());

                }
            });
        }

        /***
         * If Token process successfully then start the Video
         */
        private void setOnVideoPlayLicenseProcessListener() {

            this.OnVideoPlayLicenseListener = () -> playVideoScreen(mSelectedChapter);
        }

        /**
         * Got to video screen , before check permission
         *
         * @param mSelectedChapterInner
         */
        private void playVideoScreen(VideosWithChapter mSelectedChapterInner) {
            if ((Build.VERSION.SDK_INT >= 23)) {
                Activity activity = (Activity) context;
                boolean hasPermission = (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                if (!hasPermission) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_PERMISSION_CODE);

                } else {
                    Intent intent = new Intent(context, VideoPlayerScreenActivity.class);
                    ArrayList<Videos> allVideosForChapter = new ArrayList<>(db.getAllVideosForChapter(mSelectedChapterInner.ChapterID));
                    int mSelectedPosition = 0;
                    for (int i = 0; i < allVideosForChapter.size(); i++) {
                        Videos videos = allVideosForChapter.get(i);
                        if (videos.VideoID == mSelectedChapterInner.VideoID) {
                            mSelectedPosition = i;
                            break;
                        }
                    }
                    intent.putExtra("videos", allVideosForChapter);
                    intent.putExtra("selectedVideoPosition", mSelectedPosition);
                    context.startActivity(intent);
                }
            } else {
                Intent intent = new Intent(context, VideoPlayerScreenActivity.class);
                ArrayList<Videos> allVideosForChapter = new ArrayList<>(db.getAllVideosForChapter(mSelectedChapterInner.ChapterID));
                int mSelectedPosition = 0;
                for (int i = 0; i < allVideosForChapter.size(); i++) {
                    Videos videos = allVideosForChapter.get(i);
                    if (videos.VideoID == mSelectedChapterInner.VideoID) {
                        mSelectedPosition = i;
                        break;
                    }
                }
                intent.putExtra("videos", allVideosForChapter);
                intent.putExtra("selectedVideoPosition", mSelectedPosition);
                context.startActivity(intent);
            }
        }

        @Override
        public int getItemCount() {
            if (videosWithChapters != null) {
                return videosWithChapters.size();
            } else {
                return 0;
            }
        }

        class VideoViewHolder extends RecyclerView.ViewHolder {
            ImageView ivVideoPlay, ivVideoThumbnail;
            TextView tvVideoTitle, tvVideoDesc;
            TextView tvVideoDuration;

            public VideoViewHolder(View itemView) {
                super(itemView);
                ivVideoThumbnail = itemView.findViewById(R.id.ivVideoThumbnail);
                ivVideoPlay = itemView.findViewById(R.id.ivVideoPlay);
                tvVideoTitle = itemView.findViewById(R.id.tvVideoTitle);
                tvVideoDesc = itemView.findViewById(R.id.tvVideoDesc);
                tvVideoDuration = itemView.findViewById(R.id.tvVideoDuration);
            }
        }
    }
}