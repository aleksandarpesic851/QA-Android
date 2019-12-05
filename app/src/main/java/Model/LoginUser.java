package Model;

import java.io.Serializable;

/**
 * Created by India on 6/22/2016.
 */
public class LoginUser implements Serializable {

    private String CountryName;
    private String SubscriptionEndDate;
    private String CityName;
    private String Otp;
    private String IsOTPChecked;
    private String ChangedDeviceCount;
    private String StudentID;
    private String Status;
    private String ChangeDeviceRequest;
    private String UniqueNumber;
    private String CellPhone;
    private String Email;
    private String FullName;
    private String CreatedDate;
    private String ConcurrentDeviceCount;
    private String CountryID;
    private String SubscriptionPeriod;
    private String ReferralCode;
    private String FriendReferralCode;
    private String RegType;

    private String NewDevice; //if 0 It means the device is already register in the system ,1 means new user

    //To set and get TagID or user preference
    private String TagID;


    public String getIsOTPChecked() {
        return IsOTPChecked;
    }

    public void setIsOTPChecked(String isOTPChecked) {
        IsOTPChecked = isOTPChecked;
    }

    public String getFriendReferralCode() {
        return FriendReferralCode;
    }

    public void setFriendReferralCode(String friendReferralCode) {
        FriendReferralCode = friendReferralCode;
    }

    public String getCountryName() {
        return CountryName;
    }

    public void setCountryName(String countryName) {
        CountryName = countryName;
    }

    public String getSubscriptionEndDate() {
        return SubscriptionEndDate;
    }

    public void setSubscriptionEndDate(String subscriptionEndDate) {
        SubscriptionEndDate = subscriptionEndDate;
    }

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }

    public String getOtp() {
        return Otp;
    }

    public void setOtp(String otp) {
        Otp = otp;
    }

    public String getChangedDeviceCount() {
        return ChangedDeviceCount;
    }

    public void setChangedDeviceCount(String changedDeviceCount) {
        ChangedDeviceCount = changedDeviceCount;
    }

    public String getStudentID() {
        return StudentID;
    }

    public void setStudentID(String studentID) {
        StudentID = studentID;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getChangeDeviceRequest() {
        return ChangeDeviceRequest;
    }

    public void setChangeDeviceRequest(String changeDeviceRequest) {
        ChangeDeviceRequest = changeDeviceRequest;
    }

    public String getUniqueNumber() {
        return UniqueNumber;
    }

    public void setUniqueNumber(String uniqueNumber) {
        UniqueNumber = uniqueNumber;
    }

    public String getCellPhone() {
        return CellPhone;
    }

    public void setCellPhone(String cellPhone) {
        CellPhone = cellPhone;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        CreatedDate = createdDate;
    }

    public String getConcurrentDeviceCount() {
        return ConcurrentDeviceCount;
    }

    public void setConcurrentDeviceCount(String concurrentDeviceCount) {
        ConcurrentDeviceCount = concurrentDeviceCount;
    }

    public String getCountryID() {
        return CountryID;
    }

    public void setCountryID(String countryID) {
        CountryID = countryID;
    }

    public String getSubscriptionPeriod() {
        return SubscriptionPeriod;
    }

    public void setSubscriptionPeriod(String subscriptionPeriod) {
        SubscriptionPeriod = subscriptionPeriod;
    }

    public String getReferralCode() {
        return ReferralCode;
    }

    public void setReferralCode(String referralCode) {
        ReferralCode = referralCode;
    }

    public String getNewDevice() {
        return NewDevice;
    }

    public void setNewDevice(String newDevice) {
        NewDevice = newDevice;
    }

    public String getRegType() {
        return RegType;
    }

    public void setRegType(String regType) {
        RegType = regType;
    }

    public String getTagID() {
        return TagID;
    }

    public void setTagID(String tagID) {
        TagID = tagID;
    }
}
