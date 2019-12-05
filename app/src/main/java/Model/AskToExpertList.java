package Model;

import java.io.Serializable;

/**
 * Created by India on 6/9/2016.
 */
public class AskToExpertList implements Serializable{

    private int AskToExpertID;
    private String Title;
    private String Message;
    private String StudentID;
    private String Date;
    private String commentCount;
    private String FullName;

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    private String Rno;


    public int getAskToExpertID() {
        return AskToExpertID;
    }

    public void setAskToExpertID(int askToExpertID) {
        AskToExpertID = askToExpertID;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getStudentID() {
        return StudentID;
    }

    public void setStudentID(String studentID) {
        StudentID = studentID;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }
}
