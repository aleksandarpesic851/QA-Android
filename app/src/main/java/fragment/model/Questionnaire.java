package fragment.model;

/**
 * Created by saisasank on 6/3/18.
 */
public class Questionnaire {

    private int questionNo;
    private int answer;
    private String otherOptionAnswer;

    public int getQuestionNo() {
        return questionNo;
    }

    public void setQuestionNo(int questionNo) {
        this.questionNo = questionNo;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public String getOtherOptionAnswer() {

        if (otherOptionAnswer != null) {
            return otherOptionAnswer;
        } else {
            return "";
        }
    }

    public void setOtherOptionAnswer(String otherOptionAnswer) {
        this.otherOptionAnswer = otherOptionAnswer;
    }
}