package fragment;

import android.content.Context;
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

import com.google.gson.Gson;
import com.visualphysics.IndianFormActivity;
import com.visualphysics.R;

import UIControl.CustomTextViewRegular;
import Utils.AppUtil;
import fragment.model.Questionnaire;

import static Utils.AppUtil.hideKeyBoard;

/**
 * Created by saisasank on 6/3/18.
 */
public class Questionnaire1Fragment extends Fragment implements View.OnClickListener {

    private RelativeLayout mOption11;
    private RelativeLayout mOption12;
    private RelativeLayout mOption13;
    private RelativeLayout mOption14;

    private CustomTextViewRegular mOption11Ans;
    private CustomTextViewRegular mOption12Ans;
    private CustomTextViewRegular mOption13Ans;
    private EditText mOption14Ans;
    private Button mSubmitBtn;

    private int mSelectedOption = -1;

    public static final String OPTION = "option_1";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_questionnaire_1, container, false);

        mOption11 = (RelativeLayout) view.findViewById(R.id.option_11);
        mOption12 = view.findViewById(R.id.option_12);
        mOption13 = view.findViewById(R.id.option_13);
        mOption14 = view.findViewById(R.id.option_14);

        mOption11Ans = view.findViewById(R.id.option_11_ans);
        mOption12Ans = view.findViewById(R.id.option_12_ans);
        mOption13Ans = view.findViewById(R.id.option_13_ans);
        mOption14Ans = view.findViewById(R.id.option_14_ans);

        mSubmitBtn = view.findViewById(R.id.submit_btn);
        mSubmitBtn.setOnClickListener(this);

        mOption11.setOnClickListener(this);
        mOption12.setOnClickListener(this);
        mOption13.setOnClickListener(this);
        mOption14.setOnClickListener(this);
        mOption14Ans.setOnClickListener(this);

        ((IndianFormActivity) getActivity()).mActionBarTitle.setText(getResources().getString(R.string.personalisation_details));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mSelectedOption == 1) {
            handlingBackground(mOption11, null, true);
            mSubmitBtn.setText("Next");
        } else {
            handlingBackground(mOption11, null, false);
        }
        handlingBackground(mOption12, null, false);
        handlingBackground(mOption13, null, false);
        handlingBackground(mOption14, null, false);

        mOption14Ans.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    onClick(mOption14Ans);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        int previousSelectionOption = mSelectedOption;
        boolean isOptionSelected = false;
        switch (view.getId()) {
            case R.id.option_11:
                mSelectedOption = 1;
                isOptionSelected = true;
                mOption14Ans.setText("");
                mOption14Ans.clearFocus();
                hideKeyBoard(getActivity());
                mSubmitBtn.setText("Next");
                break;
            case R.id.option_12:
                mSelectedOption = 2;
                isOptionSelected = true;
                mOption14Ans.setText("");
                mOption14Ans.clearFocus();
                mSubmitBtn.setText("Next");
                hideKeyBoard(getActivity());
                break;
            case R.id.option_13:
                mSelectedOption = 3;
                isOptionSelected = true;
                mOption14Ans.setText("");
                mOption14Ans.clearFocus();
                mSubmitBtn.setText("Submit");
                hideKeyBoard(getActivity());
                break;
            case R.id.option_14:
                mSelectedOption = 4;
                isOptionSelected = true;
                mOption14Ans.requestFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mOption14Ans, InputMethodManager.SHOW_IMPLICIT);
                mSubmitBtn.setText("Submit");
                break;
            case R.id.option_14_ans:
                mSelectedOption = 4;
                isOptionSelected = true;
                break;
        }

        if ((isOptionSelected) && (previousSelectionOption != mSelectedOption)) {
            if (previousSelectionOption > 0) {
                handlingBackground(getBackgroundViewOfPosition(previousSelectionOption), getTextViewOfPosition(previousSelectionOption), false);
            }
            if (mSelectedOption > 0) {
                handlingBackground(getBackgroundViewOfPosition(mSelectedOption), getTextViewOfPosition(mSelectedOption), true);
            }
            return;
        }
        if (view.getId() == R.id.submit_btn) {
            if (mSubmitBtn.getText().toString().equalsIgnoreCase("Next")) {
                saveOptions();
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.llQuestionnaire, new Questionnaire2Fragment()).commit();
            } else {
                if (mSelectedOption > 0) {
                    if (mSelectedOption == 4) {
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
        }

    }

    /***
     * On Submit Form; Navigate to next screen
     */
    private void onSubmitSuccess() {

        saveOptions();

        FreeTrialFragment freeTrialFragment = new FreeTrialFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(OPTION, mSelectedOption);
        freeTrialFragment.setArguments(bundle);

        getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.llQuestionnaire, freeTrialFragment).commit();

    }


    private View getBackgroundViewOfPosition(int position) {
        View view = null;
        switch (position) {
            case 1:
                view = mOption11;
                break;
            case 2:
                view = mOption12;
                break;
            case 3:
                view = mOption13;
                break;
            case 4:
                view = mOption14;
                break;
        }

        return view;
    }

    private TextView getTextViewOfPosition(int position) {
        TextView textView = null;
        switch (position) {
            case 1:
                textView = mOption11Ans;
                break;
            case 2:
                textView = mOption12Ans;
                break;
            case 3:
                textView = mOption13Ans;
                break;
        }

        return textView;
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

    private void saveOptions() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Questionnaire", Context.MODE_PRIVATE);

        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setQuestionNo(1);
        questionnaire.setAnswer(mSelectedOption);

        if (mSelectedOption == 4) {
            questionnaire.setOtherOptionAnswer(mOption14Ans.getText().toString());
        }

         /*Added by IZISS for Mixpanel*/
        else if (mSelectedOption == 3) {
            questionnaire.setOtherOptionAnswer(mOption13Ans.getText().toString());
        } else if (mSelectedOption == 2) {
            questionnaire.setOtherOptionAnswer(mOption12Ans.getText().toString());
        } else if (mSelectedOption == 1) {
            questionnaire.setOtherOptionAnswer(mOption11Ans.getText().toString());
        }
         /*End by IZISS for Mixpanel*/

        String json = new Gson().toJson(questionnaire);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Q1", json);

        editor.commit();
    }

    private boolean checkValidation() {

        boolean status = false;
        String answer = mOption14Ans.getText().toString();
        if (!TextUtils.isEmpty(answer)) {
            status = true;
        } else {
            Toast.makeText(getActivity(), "You have to write something in Others Field", Toast.LENGTH_SHORT).show();
        }

        return status;
    }

}
