package fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.visualphysics.IndianFormActivity;
import com.visualphysics.NavigationScreen;
import com.visualphysics.OTPScreen;
import com.visualphysics.R;

import Model.LoginUser;
import Utils.SharedPrefrences;

/**
 * Created by iziss on 20/2/18.
 */
public class Form1Fragment extends Fragment implements View.OnClickListener {

    private View parentView;

    private Button btnSubmit;

    private RadioGroup rgQuestion1;
    private EditText edtOther;

    private final String NEXT = "NEXT";
    private final String SUBMIT = "SUBMIT";
    private String description = "";

    private final int OPTION_1 = 1;
    private final int OPTION_2 = 2;
    private final int OPTION_3 = 3;
    private final int OPTION_4 = 4;

    private int selectedOption = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parentView = inflater.inflate(R.layout.fragment_form1, container, false);

        mDeclaration();

        return parentView;
    }

    private void mDeclaration() {

        rgQuestion1 = (RadioGroup) parentView.findViewById(R.id.rgQuestion1);
        edtOther = (EditText) parentView.findViewById(R.id.edtOther);

        btnSubmit = (Button) parentView.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(this);

        rgQuestion1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                RadioButton mRadioButton = (RadioButton) parentView.findViewById(checkedId);
                mRadioButton.getText().toString();

                switch (checkedId) {

                    case R.id.rbOption_1:
                        selectedOption = OPTION_1;
                        break;

                    case R.id.rbOption_2:
                        selectedOption = OPTION_2;
                        break;

                    case R.id.rbOption_3:
                        selectedOption = OPTION_3;
                        break;

                    case R.id.rbOption_4:
                        selectedOption = OPTION_4;
                        break;
                }

                setText(selectedOption);

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

                if (btnSubmit.getText().toString().equalsIgnoreCase(NEXT)) {

                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.llQuestionnaire, new Form2Fragment()).commit();

                } else {

                    if (selectedOption > 0) {
                        if (selectedOption == OPTION_4) {
                            if (checkValidation()) {
                                onSubmitSuccess();
                            }
                        } else {
                            onSubmitSuccess();
                        }
                    } else {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.please_choose_an_option), Toast.LENGTH_SHORT).show();
                    }
                }

                break;
        }

    }

    /***
     * @param selectedOption
     */
    private void setText(int selectedOption) {

        if (selectedOption == OPTION_1) {
            btnSubmit.setText(NEXT);
        } else {
            btnSubmit.setText(SUBMIT);
        }

        if (selectedOption == OPTION_4) {
            edtOther.setEnabled(true);
        } else {
            edtOther.setEnabled(false);
            edtOther.setError(null);//removes error
        }

    }

    /***
     * Check the Validation of the input
     *
     * @return
     */
    private boolean checkValidation() {

        boolean status = false;
        description = edtOther.getText().toString();
        if (!TextUtils.isEmpty(description)) {
            status = true;
        } else {
            edtOther.setError(getResources().getString(R.string.error_field_required));
        }

        return status;
    }

}
