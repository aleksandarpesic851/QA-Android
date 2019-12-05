package fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.visualphysics.ApiCall;
import com.visualphysics.IndianFormActivity;
import com.visualphysics.R;

import org.json.JSONObject;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.TimeZone;

import Model.LoginUser;
import UIControl.CustomTextViewRegular;
import Utils.AppUtil;
import Utils.ErrorLog;
import Utils.OnTaskCompleted;
import Utils.SharedPrefrences;
import fragment.model.Questionnaire;
import mixpanel.MixPanelClass;

import static Utils.AppUtil.hideKeyBoard;

/**
 * Created by saisasank on 6/3/18.
 */
public class Questionnaire2Fragment extends Fragment implements View.OnClickListener {

    private RelativeLayout mOption21;
    private RelativeLayout mOption22;
    private RelativeLayout mOption23;
    private RelativeLayout mOption24;
    private RelativeLayout mOption31;
    private RelativeLayout mOption32;
    private RelativeLayout mOption33;
    private RelativeLayout mOption34;

    private CustomTextViewRegular mOption21Ans;
    private CustomTextViewRegular mOption22Ans;
    private CustomTextViewRegular mOption23Ans;
    private CustomTextViewRegular mOption24Ans;

    private CustomTextViewRegular mOption31Ans;
    private CustomTextViewRegular mOption32Ans;
    private CustomTextViewRegular mOption33Ans;
    private CustomTextViewRegular mOption34Ans;

    //private EditText mOption24Ans;
    private Button mSubmitBtn;

    private int mSelectedOption2 = -1;
    private int mSelectedOption3 = -1;

    public static final String OPTION_2 = "option_2";
    public static final String OPTION_3 = "option_3";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_questionnaire_2, container, false);

        mOption21 = view.findViewById(R.id.option_21);
        mOption22 = view.findViewById(R.id.option_22);
        mOption23 = view.findViewById(R.id.option_23);
        mOption24 = view.findViewById(R.id.option_24);
        mOption31 = view.findViewById(R.id.option_31);
        mOption32 = view.findViewById(R.id.option_32);
        mOption33 = view.findViewById(R.id.option_33);
        mOption34 = view.findViewById(R.id.option_34);

        mOption21Ans = view.findViewById(R.id.option_21_ans);
        mOption22Ans = view.findViewById(R.id.option_22_ans);
        mOption23Ans = view.findViewById(R.id.option_23_ans);
        mOption24Ans = view.findViewById(R.id.option_24_ans);
        mOption31Ans = view.findViewById(R.id.option_31_ans);
        mOption32Ans = view.findViewById(R.id.option_32_ans);
        mOption33Ans = view.findViewById(R.id.option_33_ans);
        mOption34Ans = view.findViewById(R.id.option_34_ans);

        mSubmitBtn = view.findViewById(R.id.submit_btn);
        mSubmitBtn.setOnClickListener(this);

        mOption21.setOnClickListener(this);
        mOption22.setOnClickListener(this);
        mOption23.setOnClickListener(this);
        mOption24.setOnClickListener(this);
        mOption31.setOnClickListener(this);
        mOption32.setOnClickListener(this);
        mOption33.setOnClickListener(this);
        mOption34.setOnClickListener(this);

        // mOption24Ans.setOnClickListener(this);

        ((IndianFormActivity) getActivity()).mActionBarTitle.setText(getResources().getString(R.string.personalisation_details));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        handlingBackground(mOption21, null, false);
        handlingBackground(mOption22, null, false);
        handlingBackground(mOption23, null, false);
        handlingBackground(mOption24, null, false);
        handlingBackground(mOption31, null, false);
        handlingBackground(mOption32, null, false);
        handlingBackground(mOption33, null, false);
        handlingBackground(mOption34, null, false);

    /*IZISS 05 July 2018*/
        /*mOption24Ans.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    onClick(mOption24Ans);
                }
            }
        });*/

    }

    private void handlingBackground(View view, TextView textView, boolean isSelected) {
        if (isSelected) {
            GradientDrawable background = (GradientDrawable) view.getBackground();
            background.setStroke((int) (Resources.getSystem().getDisplayMetrics().density) * 1, ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            if (textView != null) {
                textView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            }
        } else {
            GradientDrawable background = (GradientDrawable) view.getBackground();
            background.setStroke((int) (Resources.getSystem().getDisplayMetrics().density) * 1, ResourcesCompat.getColor(getResources(), R.color.grey, null));
            if (textView != null) {
                textView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black_87, null));
            }
        }
    }

    private View getBackgroundViewOfPosition(int position) {
        View view = null;
        switch (position) {
            case 1:
                view = mOption21;
                break;
            case 2:
                view = mOption22;
                break;
            case 3:
                view = mOption23;
                break;
            case 4:
                view = mOption24;
                break;
            case 5:
                view = mOption31;
                break;
            case 6:
                view = mOption32;
                break;
            case 7:
                view = mOption33;
                break;
            case 8:
                view = mOption34;
                break;
        }

        return view;
    }

    private TextView getTextViewOfPosition(int position) {
        TextView textView = null;
        switch (position) {
            case 1:
                textView = mOption21Ans;
                break;
            case 2:
                textView = mOption22Ans;
                break;
            case 3:
                textView = mOption23Ans;
                break;
            case 4:
                textView = mOption24Ans;
                break;
            case 5:
                textView = mOption31Ans;
                break;
            case 6:
                textView = mOption32Ans;
                break;
            case 7:
                textView = mOption33Ans;
                break;
            case 8:
                textView = mOption34Ans;
                break;
        }

        return textView;
    }

    @Override
    public void onClick(View view) {
        int previousSelectionOption2 = mSelectedOption2;
        boolean isOptionSelected2 = false;
        switch (view.getId()) {
            case R.id.option_21:
                mSelectedOption2 = 1;
                isOptionSelected2 = true;
                hideKeyBoard(getActivity());
                break;
            case R.id.option_22:
                mSelectedOption2 = 2;
                isOptionSelected2 = true;
                hideKeyBoard(getActivity());
                break;
            case R.id.option_23:
                mSelectedOption2 = 3;
                isOptionSelected2 = true;
                hideKeyBoard(getActivity());
                break;
            case R.id.option_24:
                mSelectedOption2 = 4;
                isOptionSelected2 = true;
                break;
            case R.id.option_24_ans:
                mSelectedOption2 = 4;
                isOptionSelected2 = true;
                break;
        }

        if ((isOptionSelected2) && (previousSelectionOption2 != mSelectedOption2)) {
            if (previousSelectionOption2 > 0) {
                handlingBackground(getBackgroundViewOfPosition(previousSelectionOption2), getTextViewOfPosition(previousSelectionOption2), false);
            }
            if (mSelectedOption2 > 0) {
                handlingBackground(getBackgroundViewOfPosition(mSelectedOption2), getTextViewOfPosition(mSelectedOption2), true);
            }
            return;
        }
        int previousSelectionOption3 = mSelectedOption3;
        boolean isOptionSelected3 = false;
        switch (view.getId()) {
            case R.id.option_31:
                isOptionSelected3 = true;
                mSelectedOption3 = 5;
                break;
            case R.id.option_32:
                isOptionSelected3 = true;
                mSelectedOption3 = 6;
                break;
            case R.id.option_33:
                isOptionSelected3 = true;
                mSelectedOption3 = 7;
                break;
            case R.id.option_34:
                isOptionSelected3 = true;
                mSelectedOption3 = 8;
                break;
        }
        if ((isOptionSelected3) && (previousSelectionOption3 != mSelectedOption3)) {
            if (previousSelectionOption3 > 0) {
                handlingBackground(getBackgroundViewOfPosition(previousSelectionOption3), getTextViewOfPosition(previousSelectionOption3), false);
            }
            if (mSelectedOption3 > 0) {
                handlingBackground(getBackgroundViewOfPosition(mSelectedOption3), getTextViewOfPosition(mSelectedOption3), true);
            }
            return;
        }

        if (view.getId() == R.id.submit_btn) {
            if (mSelectedOption2 > 0 && mSelectedOption3 > 0) {
                if (mSelectedOption2 == 4) {
                    //IZISS
                   // if (checkValidation()) {
                        onSubmitSuccess();
                   // }
                } else {
                    onSubmitSuccess();
                }

            } else {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.please_choose_an_option), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /***
     * Check the Validation of the input
     *
     * @return
     */
    private boolean checkValidation() {

        boolean status = false;

        String description2 = mOption24Ans.getText().toString();

        if (!TextUtils.isEmpty(description2)) {
            status = true;
        } else {
            Toast.makeText(getActivity(), "You have to write something in Others Field", Toast.LENGTH_SHORT).show();
        }


        return status;
    }


    /***
     * On Submit Form; Navigate to next screen
     */
    private void onSubmitSuccess() {

        saveOptions();

        FreeTrialFragment freeTrialFragment = new FreeTrialFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Questionnaire1Fragment.OPTION, 1);
        bundle.putInt(Questionnaire2Fragment.OPTION_2, mSelectedOption2);
        bundle.putInt(Questionnaire2Fragment.OPTION_3, mSelectedOption3 - 4);
        freeTrialFragment.setArguments(bundle);

        getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.llQuestionnaire, freeTrialFragment).commit();

    }

    private void saveOptions() {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Questionnaire", Context.MODE_PRIVATE);

        Questionnaire questionnaire2 = new Questionnaire();
        questionnaire2.setQuestionNo(2);
        questionnaire2.setAnswer(mSelectedOption2);

        if (mSelectedOption2 == 4) {
            questionnaire2.setOtherOptionAnswer(mOption24Ans.getText().toString());
        }

         /*Added by IZISS for Mixpanel*/
        else if (mSelectedOption2 == 3) {
            questionnaire2.setOtherOptionAnswer(mOption23Ans.getText().toString());
        } else if (mSelectedOption2 == 2) {
            questionnaire2.setOtherOptionAnswer(mOption22Ans.getText().toString());
        } else if (mSelectedOption2 == 1) {
            questionnaire2.setOtherOptionAnswer(mOption21Ans.getText().toString());
        }
         /*End by IZISS for Mixpanel*/

        Questionnaire questionnaire3 = new Questionnaire();
        questionnaire3.setQuestionNo(3);
        questionnaire3.setAnswer(mSelectedOption3 - 4);

        /*Added by IZISS for Mixpanel*/
        int selectedOption3WithActualPosition = mSelectedOption3 - 4; //Because options assigned from 5

        if (selectedOption3WithActualPosition == 4) {
            questionnaire3.setOtherOptionAnswer(mOption34Ans.getText().toString());
        } else if (selectedOption3WithActualPosition == 3) {
            questionnaire3.setOtherOptionAnswer(mOption33Ans.getText().toString());
        } else if (selectedOption3WithActualPosition == 2) {
            questionnaire3.setOtherOptionAnswer(mOption32Ans.getText().toString());
        } else if (selectedOption3WithActualPosition == 1) {
            questionnaire3.setOtherOptionAnswer(mOption31Ans.getText().toString());
        }
         /*End by IZISS for Mixpanel*/

        String q2 = new Gson().toJson(questionnaire2);
        String q3 = new Gson().toJson(questionnaire3);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Q2", q2);
        editor.putString("Q3", q3);

        editor.commit();


    }
}
