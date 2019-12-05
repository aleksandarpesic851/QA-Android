package Model;

/**
 * Created by admin on 5/19/2016.
 */
public class HomeScreenFrag_Model {


    int mCategoryId;
    String mCategoryName;

    public HomeScreenFrag_Model(int categoryId, String categoryName) {
        mCategoryId = categoryId;
        mCategoryName = categoryName;
    }

    public static class FrictionCategory {

        int mFrictionId;
        String mFrictionName;

        public FrictionCategory(int FrictionId,String FrictionName)
        {
            this.mFrictionId=FrictionId;
            this.mFrictionName=FrictionName;
        }
        public int getFrictionId()
        {
            return mFrictionId;
        }
        public String getmFrictionName()
        {
            return mFrictionName;
        }
    }
    public static class FluidsCategory {

        int mFluidId;
        String mFluidName;

        public FluidsCategory(int FluidId,String FluidName)
        {
            this.mFluidId=FluidId;
            this.mFluidName=FluidName;
        }
        public int getFrictionId()
        {
            return mFluidId;
        }
        public String getmFrictionName()
        {
            return mFluidName;
        }
    }


    public int getCategoryId()
    {
        return mCategoryId;
    }
    public String getCategoryName()
    {
        return mCategoryName;
    }


}
