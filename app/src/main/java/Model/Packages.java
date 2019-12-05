package Model;

/**
 * Created by India on 7/13/2016.
 */
public class Packages {

    private int PackageID;
    private String PackageName;
    private String Description;
    private String Period;
    private String OfferPrice;
    private String OriginalPrice;
    private String USDOfferPrice;

    //New
    private String OfferPeriod;
    private String OriginalUSDPrice = "0";

    private boolean isChecked = false;

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getPackageID() {
        return PackageID;
    }

    public void setPackageID(int packageID) {
        PackageID = packageID;
    }

    public String getPackageName() {
        return PackageName;
    }

    public void setPackageName(String packageName) {
        PackageName = packageName;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getPeriod() {
        return Period;
    }

    public void setPeriod(String period) {
        Period = period;
    }

    public String getOfferPrice() {
        return OfferPrice;
    }

    public void setOfferPrice(String offerPrice) {
        OfferPrice = offerPrice;
    }

    public String getOriginalPrice() {
        return OriginalPrice;
    }

    public void setOriginalPrice(String originalPrice) {
        OriginalPrice = originalPrice;
    }

    public String getUSDOfferPrice() {
        return USDOfferPrice;
    }

    public void setUSDOfferPrice(String USDOfferPrice) {
        this.USDOfferPrice = USDOfferPrice;
    }

    public String getOfferPeriod() {
        return OfferPeriod;
    }

    public void setOfferPeriod(String offerPeriod) {
        OfferPeriod = offerPeriod;
    }

    public String getOriginalUSDPrice() {
        return OriginalUSDPrice;
    }

    public void setOriginalUSDPrice(String originalUSDPrice) {
        OriginalUSDPrice = originalUSDPrice;
    }
}
