package fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.visualphysics.ApiCall;
import com.visualphysics.IndianFormActivity;
import com.visualphysics.NavigationScreen;
import com.visualphysics.OTPScreen;
import com.visualphysics.R;

import java.util.ArrayList;

import Adapter.PackageAdapter;
import Model.LoginUser;
import Model.Packages;
import Utils.AppUtil;
import Utils.SharedPrefrences;


public class Form2Fragment extends Fragment implements View.OnClickListener {

    private View parentView;

    private Button btnBack, btnSubmit;

    private RadioGroup rgQuestion2, rgQuestion3;

    private EditText edtOther2, edtOther3;
    private String description2 = "", description3 = "";

    ScrollView scrollView;

    private final int OPTION_1 = 1;
    private final int OPTION_2 = 2;
    private final int OPTION_3 = 3;
    private final int OPTION_4 = 4;

    private final int OPTION_5 = 5;
    private final int OPTION_6 = 6;
    private final int OPTION_7 = 7;
    private final int OPTION_8 = 8;

    private int selectedOption2 = 0, selectedOption3 = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parentView = inflater.inflate(R.layout.fragment_form2, container, false);

        mDeclaration();

        return parentView;
    }

    private void mDeclaration() {

        rgQuestion2 = (RadioGroup) parentView.findViewById(R.id.rgQuestion2);
        rgQuestion3 = (RadioGroup) parentView.findViewById(R.id.rgQuestion3);


        edtOther2 = (EditText) parentView.findViewById(R.id.edtOther2);
        edtOther3 = (EditText) parentView.findViewById(R.id.edtOther3);

        scrollView = (ScrollView) parentView.findViewById(R.id.scrollView);

        btnBack = (Button) parentView.findViewById(R.id.btnBack);
        btnSubmit = (Button) parentView.findViewById(R.id.btnSubmit);

        btnBack.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);

        rgQuestion2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                RadioButton mRadioButton = (RadioButton) parentView.findViewById(checkedId);
                mRadioButton.getText().toString();

                switch (checkedId) {

                    case R.id.rbOption_21:
                        selectedOption2 = OPTION_1;
                        break;

                    case R.id.rbOption_22:
                        selectedOption2 = OPTION_2;
                        break;

                    case R.id.rbOption_23:
                        selectedOption2 = OPTION_3;
                        break;

                    case R.id.rbOption_24:
                        selectedOption2 = OPTION_4;
                        break;
                }

                setTextFor2(selectedOption2);

            }
        });

        rgQuestion3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                RadioButton mRadioButton = (RadioButton) parentView.findViewById(checkedId);
                mRadioButton.getText().toString();

                switch (checkedId) {

                    case R.id.rbOption_31:
                        selectedOption3 = OPTION_5;
                        break;

                    case R.id.rbOption_32:
                        selectedOption3 = OPTION_6;
                        break;

                    case R.id.rbOption_33:
                        selectedOption3 = OPTION_7;
                        break;

                    case R.id.rbOption_34:
                        selectedOption3 = OPTION_8;
                        break;
                }

                setTextFor3(selectedOption3);

            }
        });


    }

    /***
     * On Submit Form; Navigate to next screen
     */
    private void onSubmitSuccess() {

        SharedPrefrences mSharePref = new SharedPrefrences(getActivity());

        LoginUser User = mSharePref.getLoginUser();

        if (IndianFormActivity.isReferralCodeApplied)
            User.setFriendReferralCode(IndianFormActivity.strReferralCode);

        //Update NewDevice to Null so that at the Time of Splash Screen(Ref MoveTONextScreen()) consider as old device
        User.setNewDevice(null);

        Gson gson = new Gson();
        String jsonString = gson.toJson(User);

        //Update User Detail
        mSharePref.setPreferences(mSharePref.USERDETAIL, jsonString);

        //Intent to start new activity
        Intent intent;

        if (User.getIsOTPChecked().equals("0")) {
            intent = new Intent(getActivity(), OTPScreen.class);
            startActivity(intent);
            getActivity().finish();
        } else {
            intent = new Intent(getActivity(), NavigationScreen.class);
            startActivity(intent);
            getActivity().finish();
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnSubmit:
                if (selectedOption2 > 0 && selectedOption3 > 0) {

                    if (selectedOption2 == OPTION_4 && selectedOption3 == OPTION_8) {
                        if (checkValidation(false) && checkValidation(true)) {
                            onSubmitSuccess();
                        }
                    } else if (selectedOption2 == OPTION_4) {
                        if (checkValidation(false)) {
                            onSubmitSuccess();
                        }
                    } else if (selectedOption3 == OPTION_8) {
                        if (checkValidation(true)) {
                            onSubmitSuccess();
                        }
                    } else {
                        onSubmitSuccess();
                    }

                } else {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.please_choose_an_option), Toast.LENGTH_SHORT).show();
                }
                break;


            case R.id.btnBack:

                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.llQuestionnaire, new Form1Fragment()).commit();

                break;
        }

    }

    /***
     * @param selectedOption
     */
    private void setTextFor2(int selectedOption) {

        if (selectedOption == OPTION_4) {
            edtOther2.setEnabled(true);
        } else {
            edtOther2.setEnabled(false);
            edtOther2.setError(null);//removes error
        }

    }


    /***
     * @param selectedOption
     */
    private void setTextFor3(int selectedOption) {

        if (selectedOption == OPTION_8) {
            edtOther3.setEnabled(true);

            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });

        } else {
            edtOther3.setEnabled(false);
            edtOther3.setError(null);//removes error
        }
    }

    /***
     * Check the Validation of the input
     *
     * @return
     */
    private boolean checkValidation(boolean isThird) {

        boolean status = false;

        if (isThird) {

            description3 = edtOther3.getText().toString();

            if (!TextUtils.isEmpty(description3)) {
                status = true;
            } else {
                edtOther3.setError(getResources().getString(R.string.error_field_required));
            }
        } else {
            description2 = edtOther2.getText().toString();

            if (!TextUtils.isEmpty(description2)) {
                status = true;
            } else {
                edtOther2.setError(getResources().getString(R.string.error_field_required));

            }
        }

        return status;
    }

}
