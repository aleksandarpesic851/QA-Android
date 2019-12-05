package buy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.visualphysics.R;

import java.util.ArrayList;

import Model.Packages;
import Utils.AppUtil;

/**
 * Created by admin on 5/19/2016.
 */
@SuppressWarnings("All")
public class BuyTabFragment extends Fragment implements OnPackageSelectionListener {

    private View parentView;
    private RecyclerView mPackageRecyclerView;

    private ArrayList<Packages> mPackageArrayList;
    private BuyPackageAdapter mPackageAdapter;
    private String currencyType;

    //To handle click response on next page
    private AppUtil mAppUtils;
    private int SUCCESS_REQUEST_CODE = 101;
    private OnPackageSelectionListener mOnPackageSelectionListener;

    public static boolean isReload = false;

    public BuyTabFragment(ArrayList<Packages> mPackageArrayList, String currencyType, OnPackageSelectionListener mOnPackageSelectionListener) {

        this.mPackageArrayList = mPackageArrayList;
        this.currencyType = currencyType;
        this.mOnPackageSelectionListener = mOnPackageSelectionListener;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parentView = inflater.inflate(R.layout.fragment_buy_tab, container, false);

        mDeclaration();

        return parentView;
    }

    /***
     * Initialize the resources
     */
    private void mDeclaration() {

        mAppUtils = new AppUtil(getActivity());

        mPackageRecyclerView = (RecyclerView) parentView.findViewById(R.id.recyclerViewPackageScreen);
        mPackageRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 3. create an adapter
        mPackageAdapter = new BuyPackageAdapter(mPackageArrayList, getActivity(), currencyType, mOnPackageSelectionListener, this);

        // 4. set adapter
        mPackageRecyclerView.setAdapter(mPackageAdapter);

    }

    /***
     * This will return buypager object
     *
     * @return
     */
    private BuyPackageAdapter getAdapter() {

        if (mPackageAdapter != null) {
            return mPackageAdapter;
        }
        return null;
    }


    @Override
    public void onPackageSelected(Packages mPackages, int position, String currencyType) {

        BuyTabFragment.isReload = true;

    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && BuyTabFragment.isReload) {

            if (mPackageAdapter != null) {
                mPackageAdapter.refreshList();
            }

            BuyTabFragment.isReload = false;

        }

    }


}
