package Model;

import java.io.Serializable;

/**
 * Created by India on 6/9/2016.
 */
public class AskToExpertDiscusssionList implements Serializable{

    private int AskToExpertID;

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    private String Title,
            Message,
            StudentID,
            Date,
            UserType,
            ResponseDate,
            UserID,
            Response,
            AskToExpertResponseID,
            FullName,
            commentCount;

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

    public String getUserType() {
        return UserType;
    }

    public void setUserType(String userType) {
        UserType = userType;
    }

    public String getResponseDate() {
        return ResponseDate;
    }

    public void setResponseDate(String responseDate) {
        ResponseDate = responseDate;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getResponse() {
        return Response;
    }

    public void setResponse(String response) {
        Response = response;
    }

    public String getAskToExpertResponseID() {
        return AskToExpertResponseID;
    }

    public void setAskToExpertResponseID(String askToExpertResponseID) {
        AskToExpertResponseID = askToExpertResponseID;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }
}
