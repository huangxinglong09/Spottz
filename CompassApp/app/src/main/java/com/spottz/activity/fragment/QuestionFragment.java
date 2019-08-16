package com.spottz.activity.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.spottz.R;
import com.spottz.activity.MainActivity;
import com.spottz.app.SpottzApplication;
import com.spottz.constant.Constants;
import com.spottz.model.AnswerModel;
import com.spottz.model.QuestionModel;
import com.spottz.util.Utils;

import java.util.ArrayList;

public class QuestionFragment extends Fragment implements View.OnClickListener {

    private TextView lblNavTitle, lblStep, lblTitle, lblQuestion;
    private ImageButton btnMenu, btnProfile;
    private ImageView ivImage;
    private String strCategoryID;
    private View viewResult;
    private TextView lblResultStep;
    private TextView lblResTitle, lblResResult, lblResQuestion, lblResAnswer, lblDesc;
    private TextView lblContent;
    private Button btnViewCompass;
    private ListView lstAnswer;
    private AnswerAdapter adapter;
    private ScrollView viewQuestion;

    private ArrayList<AnswerModel> arrAnswers;
    private QuestionModel questionInfo;
    private ImageView ivPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lblNavTitle = (TextView) view.findViewById(R.id.lblNavTitle);
        btnMenu = (ImageButton) view.findViewById(R.id.btnMenu);
        btnProfile = (ImageButton) view.findViewById(R.id.btnProfile);
        lblStep = (TextView) view.findViewById(R.id.lblStep);
        lblTitle = (TextView) view.findViewById(R.id.lblTitle);
        lblContent = (TextView) view.findViewById(R.id.lblContent);
        ivImage = (ImageView) view.findViewById(R.id.ivImage);
        lblQuestion = (TextView) view.findViewById(R.id.lblQuestion);

        lstAnswer = (ListView) view.findViewById(R.id.lstAnswers);

        questionInfo = SpottzApplication.getInstance().getCurrentQuestion();
        arrAnswers = questionInfo.arrAnswers;
        strCategoryID = String.valueOf(SpottzApplication.getInstance().currentItem.iID);

        adapter = new AnswerAdapter(this.getActivity(), R.layout.item_answer);
        lstAnswer.setAdapter(adapter);
        // Result View
        viewResult = view.findViewById(R.id.viewResult);
        lblResultStep = (TextView) view.findViewById(R.id.lblResultStep);
        lblResTitle = (TextView) view.findViewById(R.id.lblResTitle);
        lblResResult = (TextView) view.findViewById(R.id.lblResResult);
        lblResQuestion = (TextView) view.findViewById(R.id.lblResQuestion);
        lblResAnswer = (TextView) view.findViewById(R.id.lblResAnswer);
        lblDesc = (TextView) view.findViewById(R.id.lblDesc);
        btnViewCompass = (Button) view.findViewById(R.id.btnViewCompass);
        btnViewCompass.setOnClickListener(this);
        viewQuestion = (ScrollView) view.findViewById(R.id.viewQuestion);
        viewQuestion.setVisibility(View.VISIBLE);
        viewResult.setVisibility(View.INVISIBLE);
        ivPreview = (ImageView) view.findViewById(R.id.ivPreview);

        lstAnswer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showResult(position);
            }
        });

        this.showQuestion();
        this.setFonts();
    }

    private void setFonts() {
        Utils.setMedium(lblStep);
        Utils.setMedium(lblResultStep);
        Utils.setBold(lblResTitle);
        Utils.setSemiBold(lblResResult);
        Utils.setBold(lblResQuestion);
        Utils.setBold(lblResAnswer);
        Utils.setMedium(lblDesc);
        Utils.setBold(lblTitle);
        Utils.setMedium(lblContent);
        Utils.setSemiBold(lblQuestion);
        Utils.setBold(btnViewCompass);
    }

    private void showNextQuestion() {
        MainActivity activity = (MainActivity) getActivity();
        if (SpottzApplication.getInstance().isFinalSpot()) {
            activity.showFragment(Constants.INT_FRMT_RESULT);
        } else {
            SpottzApplication.getInstance().iCurrentSpot++;
            activity.showFragment(Constants.INT_FRMT_ROUTE);
        }
    }

    void showQuestion() {
        questionInfo.endTime = System.currentTimeMillis();
        viewResult.setVisibility(View.GONE);
        lblTitle.setText(questionInfo.strSpotName);
        lblResTitle.setText(questionInfo.strSpotName);
        int currentQuestionIdx = SpottzApplication.getInstance().iCurrentSpot;
        lblStep.setText(String.format("%d van %d", currentQuestionIdx + 1, SpottzApplication.getInstance().curSpots.arrQuestoins.size()));
        lblResultStep.setText(String.format("%d van %d", currentQuestionIdx + 1, SpottzApplication.getInstance().curSpots.arrQuestoins.size()));

        Utils.loadImage(this.getActivity(), ivImage, questionInfo.getImageURL());
        Utils.loadImage(this.getActivity(), ivPreview, questionInfo.strCompanyImage);
        lblContent.setText(questionInfo.strContent);
        lblQuestion.setText(questionInfo.strQuestion);
        lblResQuestion.setText(questionInfo.strQuestion);

        ViewGroup.LayoutParams params = lstAnswer.getLayoutParams();
        int height = (int) (getResources().getDimension(R.dimen.height_answer_item) * questionInfo.arrAnswers.size() + 30);
        params.height = height;
        lstAnswer.setLayoutParams(params);
        lstAnswer.requestLayout();
        adapter.notifyDataSetChanged();
        viewQuestion.scrollTo(0, 0);
    }

    void showResult(int ansidx) {

        // Check answer has to be show or not
        boolean bSkipShowAnswer = false;

        questionInfo.iSelectedAnswer = ansidx;

        AnswerModel answer = questionInfo.arrAnswers.get(ansidx);

        if (answer.bRight) {
            // Your Answer is right.
            lblResResult.setText("JUIST"); // Good
            lblResResult.setTextColor(getResources().getColor(R.color.green));
            lblDesc.setText(questionInfo.strCorrectAnswer);

            if (TextUtils.isEmpty(questionInfo.strCorrectAnswer)) {
                bSkipShowAnswer = true;
            }
        } else {
            // Your Answer is wrong.
            lblResResult.setText("ONJUIST"); // Wrong
            lblResResult.setTextColor(getResources().getColor(R.color.red));
            lblDesc.setText(questionInfo.strWrongAnswer);

            if (TextUtils.isEmpty(questionInfo.strWrongAnswer)) {
                bSkipShowAnswer = true;
            }
        }

        for (int idx = 0; idx < questionInfo.arrAnswers.size(); idx++) {
            AnswerModel ans = questionInfo.arrAnswers.get(idx);
            if (ans.bRight) {
                lblResAnswer.setText(ans.strTitle);
                break;
            }
        }

        viewResult.setVisibility(View.VISIBLE);
        if (SpottzApplication.getInstance().isFinalSpot()) {
            btnViewCompass.setText("Toon resultaat"); // show result
        } else {
            btnViewCompass.setText("Volgende"); // next
        }

        // No need to show Answer, then go to next screen
        if (bSkipShowAnswer) {
            showNextQuestion();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnViewCompass) {
            showNextQuestion();
        }
    }

    class ViewHolder {

        private TextView lblTitle;

        public ViewHolder(View itemView) {
            lblTitle = (TextView) itemView.findViewById(R.id.lblTitle);
            Utils.setSemiBold(lblTitle);
        }
    }

    class AnswerAdapter extends ArrayAdapter {
        public AnswerAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            if (arrAnswers == null)
                return 0;

            return arrAnswers.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_answer, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AnswerModel info = arrAnswers.get(position);
            String str = info.strTitle;
            holder.lblTitle.setText(info.strTitle);
            return convertView;
        }
    }

}
