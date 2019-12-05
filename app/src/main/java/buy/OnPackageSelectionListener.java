package buy;

import Model.Packages;

/**
 * Created by iziss on 28/5/18.
 */
public interface OnPackageSelectionListener {

    public void onPackageSelected(Packages mPackages, int position,String currencyType);
}
