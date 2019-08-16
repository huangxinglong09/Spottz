package com.spottz.activity.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.spottz.R;
import com.spottz.activity.MainActivity;
import com.spottz.app.SpottzApplication;
import com.spottz.constant.Constants;
import com.spottz.model.CategoryModel;
import com.spottz.model.LocalScoreModel;
import com.spottz.model.ScoreModel;
import com.spottz.net.BaseJsonRes;
import com.spottz.net.NetClient;
import com.spottz.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ScoreFragment extends Fragment {

    private GridView lstList;
    private ScoreAdapter adapter;
    private ArrayList<ScoreModel> arrResult = new ArrayList<>();
    private ProgressBar progressBar;
    private NetClient netClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_score, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnBack = (Button) view.findViewById(R.id.btnBack);
        btnBack.setText(" <    Terug naar overzicht");
        Utils.setMedium(btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.showFragment(Constants.INT_FRMT_CATEGORY_LIST);
            }
        });

        netClient = new NetClient(this.getActivity());
        progressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        lstList = (GridView)view.findViewById(R.id.lstList);
        adapter = new ScoreAdapter(getActivity(), R.layout.item_score);
        lstList.setAdapter(adapter);
        this.setFonts();
        this.loadScores();
    }

    private void loadScores() {

        final int catid = SpottzApplication.getInstance().currentItem.iID;
        String url = Utils.getAppURL("/lappapi/get/scoreall/?device_token=") + SpottzApplication.getInstance().strUDID + "&category_id=" + SpottzApplication.getInstance().currentItem.iID;
        netClient.get(url, null, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {
                progressBar.setVisibility(View.GONE);
                JSONArray array = (JSONArray) response;
                JSONObject item;
                for (int i = 0; i < array.length(); i++) {
                    item = array.optJSONObject(i);
                    if (!item.has("category"))
                        continue;

                    CategoryModel category = new CategoryModel(item.optJSONObject("category"), true);
                    if (category.iID != catid)
                        continue;

                    JSONArray arrScores = item.optJSONArray("scores");
                    if (arrScores != null) {
                        for (int idx = 0; idx < arrScores.length(); idx++) {
                            arrResult.add(new ScoreModel(arrScores.optJSONObject(idx)));
                        }
                    }
                    break;
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onMyFailure(int status) {
                progressBar.setVisibility(View.GONE);

            }
        });
    }

    private void setFonts() {

    }

    class ViewHolder {

        private ImageView ivProfile;
        private TextView lblTitle;

        public ViewHolder(View itemView) {
            lblTitle = (TextView) itemView.findViewById(R.id.lblTitle);
            ivProfile = (ImageView) itemView.findViewById(R.id.ivProfile);
        }
    }

    class ScoreAdapter extends ArrayAdapter
    {
        public ScoreAdapter(Context context, int resource)
        {
            super(context, resource);
        }

        @Override
        public int getCount()
        {
            if (arrResult == null)
                return 0;
            return arrResult.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            ScoreModel info = arrResult.get(position);
            holder.lblTitle.setText(info.getScoreToString());
            Utils.loadAvatarImage(getContext(), holder.ivProfile, info.getImageURL());
            return convertView;
        }
    }
}
