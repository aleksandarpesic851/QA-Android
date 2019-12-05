package Model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

/**
 * Created by India on 6/9/2016.
 */
@Table(name = "ChapterVideoDetail")
public class ChapterVideoDetail extends Model implements Serializable {

    @Column(name = "VideoID", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private int VideoID;

    @Column(name = "ChapterID")
    private int ChapterID;

    @Column(name = "UserID")
    private String UserID;

    @Column(name = "LikeStatus")
    private boolean LikeStatus = false;

    @Column(name = "UploadLikeStatus")
    private boolean UploadLikeStatus = false;

    @Column(name = "VideoDownloadCount")
    private int VideoDownloadCount = 0;

    @Column(name = "VideoPlayCount")
    private int VideoPlayCount = 0;


    public ChapterVideoDetail() {
        super();
    }

    public ChapterVideoDetail(int VideoID, int ChapterID, String UserID) {
        super();
        this.VideoID = VideoID;
        this.ChapterID = ChapterID;
        this.UserID = UserID;
    }


    public boolean isUploadLikeStatus() {
        return UploadLikeStatus;
    }

    public void setUploadLikeStatus(boolean uploadLikeStatus) {
        UploadLikeStatus = uploadLikeStatus;
    }

    public int getVideoDownloadCount() {
        return VideoDownloadCount;
    }

    public void setVideoDownloadCount(int videoDownloadCount) {
        VideoDownloadCount = videoDownloadCount;
    }

    public int getVideoPlayCount() {
        return VideoPlayCount;
    }

    public void setVideoPlayCount(int videoPlayCount) {
        VideoPlayCount = videoPlayCount;
    }


    public int getVideoID() {
        return VideoID;
    }

    public void setVideoID(int videoID) {
        VideoID = videoID;
    }

    public int getChapterID() {
        return ChapterID;
    }

    public void setChapterID(int chapterID) {
        ChapterID = chapterID;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public boolean isLikeStatus() {
        return LikeStatus;
    }

    public void setLikeStatus(boolean likeStatus) {
        LikeStatus = likeStatus;
    }

    public boolean isUploadStatus() {
        return UploadLikeStatus;
    }

    public void setUploadStatus(boolean uploadStatus) {
        UploadLikeStatus = uploadStatus;
    }
}
