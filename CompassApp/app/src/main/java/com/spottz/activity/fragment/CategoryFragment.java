package com.spottz.activity.fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.spottz.R;
import com.spottz.activity.MainActivity;
import com.spottz.app.SpottzApplication;
import com.spottz.constant.Constants;
import com.spottz.model.CategoryModel;
import com.spottz.model.LocalScoreModel;
import com.spottz.net.NetClient;
import com.spottz.util.Utils;

import java.util.ArrayList;

public class CategoryFragment extends Fragment {

    int mCategoryMode;
    private ListView lstCategory;
    private NetClient netClient;
    private ArrayList<CategoryModel> arrResult = new ArrayList<>();
    private CategoryAdapter adapter;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.INT_UPDATED_LOCATION_CHANGED) {
                refreshListView();
            }
        }
    };

    private Handler initializeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.INT_LOADED_CATEGORY_LIST) {
                refreshListView();
                SpottzApplication.getInstance().loadCompletedScores();
            } else if(msg.what == Constants.INT_LOADED_SCORE) {
                updateCategoryList();
            }
        }
    };

    private View headerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.mCategoryMode = getArguments().getInt("category_mode");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        SpottzApplication.getInstance().handlerCategory = initializeHandler;
        headerView = inflater.inflate(R.layout.category_header, null);
        return inflater.inflate(R.layout.fragment_category, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lstCategory = (ListView) view.findViewById(R.id.lstList);
        lstCategory.addHeaderView(headerView);
        adapter = new CategoryAdapter(getActivity(), R.layout.item_category);
        lstCategory.setAdapter(adapter);
        this.refreshListView();
        lstCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (id == -1)
                    return;

                CategoryModel item = arrResult.get((int) id);
                gotoNextScreen(item);
            }
        });
        TextView lblTitle = (TextView) headerView.findViewById(R.id.lblTitle);
        if(mCategoryMode == 0)
            lblTitle.setText("Kies een route");
        else
            lblTitle.setText("Mijn routes");
        Utils.setBold(lblTitle);
        //this.refreshListView();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCategoryList();
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove Handler
        if (SpottzApplication.getInstance().handlerLocationChanged == handler) {
            SpottzApplication.getInstance().handlerLocationChanged = null;
        }
        if (SpottzApplication.getInstance().handlerCategory == initializeHandler) {
            SpottzApplication.getInstance().handlerCategory = null;
        }
    }

    private void gotoNextScreen(CategoryModel item) {
        SpottzApplication.getInstance().currentItem = item;
        MainActivity activity = (MainActivity) getActivity();
        if (item.bCompleted) {
            activity.showFragment(Constants.INT_FRMT_SCORE);
        } else {
            activity.showFragment(Constants.INT_FRMT_CATEGORY_DETAIL);
        }
    }

    private synchronized void updateCategoryList() {
        LocalScoreModel item;
        synchronized (arrResult) {
            for (int i = 0; i < arrResult.size(); i++) {
                CategoryModel tmpCheck = arrResult.get(i);
                if (tmpCheck.bCompleted)
                    continue;

                item = SpottzApplication.getInstance().getScoreInfo(tmpCheck.iID);
                if (item != null) {
                    tmpCheck.bCompleted = true;
                    tmpCheck.score = item.score;
                    tmpCheck.time = item.date;
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // Sort current category list by Distance
    private synchronized void refreshListView() {
        synchronized (arrResult) {
            Location curLocation = SpottzApplication.getInstance().curLocation;
            arrResult.clear();
            ArrayList<CategoryModel> arrTmp = SpottzApplication.getInstance().arrCategory;
            for (CategoryModel model : arrTmp) {
                model.calculateDistance(curLocation);
            }

            for (int idx = 0; idx < arrTmp.size(); idx++) {

                CategoryModel tmp = arrTmp.get(idx);

                if(mCategoryMode == 1)
                    if(!tmp.bCompleted)
                        continue;

                boolean addedflag = false;
                for (int i = 0; i < arrResult.size(); i++) {

                    CategoryModel tmpCheck = arrResult.get(i);
                    if (tmp.distance < tmpCheck.distance) {
                        addedflag = true;
                        arrResult.add(i, tmp);
                        break;
                    }
                }

                if (!addedflag)
                    arrResult.add(tmp);
            }
        }

        adapter.notifyDataSetChanged();
    }

    class ViewHolder {

        private ImageView ivCategory;
        private TextView lblType;
        private ImageView ivLock;
        private TextView lblTitle, lblPlace, lblDistance, lblShortDesc;

        public ViewHolder(View itemView) {
            ivCategory = (ImageView) itemView.findViewById(R.id.ivCategory);
            lblType = (TextView) itemView.findViewById(R.id.lblType);
            ivLock = (ImageView) itemView.findViewById(R.id.ivLock);
            lblTitle = (TextView) itemView.findViewById(R.id.lblTitle);
            lblPlace = (TextView) itemView.findViewById(R.id.lblPlace);
            lblDistance = (TextView) itemView.findViewById(R.id.lblDistance);
            lblShortDesc = (TextView) itemView.findViewById(R.id.lblShortDesc);
            this.setFonts();
        }
        private void setFonts() {
            Utils.setSemiBold(lblTitle);
            Utils.setSemiBold(lblPlace);
            Utils.setMedium(lblShortDesc);
            Utils.setSemiBold(lblDistance);
        }
    }

    class CategoryAdapter extends ArrayAdapter {
        public CategoryAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            if (arrResult == null)
                return 0;
            return arrResult.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            CategoryModel info = arrResult.get(position);

            if (info.type.equals("paid")) {
                holder.ivLock.setVisibility(View.GONE);
                holder.lblType.setText(String.format("€%.02f", info.price));
            } else if (info.type.equals("code")) {
                holder.ivLock.setVisibility(View.VISIBLE);
                holder.lblType.setText("   ");
            } else {
                holder.ivLock.setVisibility(View.GONE);
                holder.lblType.setText("gratis");
            }

            if (info.bCompleted) {
                String str = String.format("Score: %d  -  %s", info.score, info.time);
                holder.lblDistance.setText(str);
                holder.lblDistance.setTextColor(getResources().getColor(R.color.red));
            } else {
                holder.lblDistance.setText(info.getDistance(SpottzApplication.getInstance().curLocation));
                holder.lblDistance.setTextColor(getResources().getColor(R.color.colorPrimary));
            }

            holder.lblPlace.setText(info.strPlace);
            holder.lblShortDesc.setText(info.strShortDesc);
            holder.lblTitle.setText(info.strTitle);
            Utils.loadCategoryThumbImage(getContext(), holder.ivCategory, info.getImageURL());
            return convertView;
        }
    }
}
