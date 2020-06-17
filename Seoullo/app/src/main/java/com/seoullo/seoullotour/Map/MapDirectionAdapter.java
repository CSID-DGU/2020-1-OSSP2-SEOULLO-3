package com.seoullo.seoullotour.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.seoullo.seoullotour.R;

import java.util.ArrayList;

import cz.msebera.android.httpclient.client.utils.CloneUtils;

public class MapDirectionAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> array_route;
    private ViewHolder mViewHolder;

    public MapDirectionAdapter(Context mContext, ArrayList<String> array_route) throws CloneNotSupportedException {
        this.mContext = mContext;
        this.array_route = (ArrayList<String>) CloneUtils.clone(array_route);

    }
    @Override
    public int getCount() {
        return array_route.size();
    }

    @Override
    public Object getItem(int position) {
        return array_route.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override

    public View getView(int position, View convertView, ViewGroup parent) {
        // ViewHoldr 패턴
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_list_item, parent, false);
            mViewHolder = new ViewHolder(convertView);
            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();

        }
        // View에 Data 세팅

        mViewHolder.txt_name.setText(array_route.get(position));


        return convertView;

    }

    public class ViewHolder {
        private TextView txt_name;
        public ViewHolder(View convertView) {
            txt_name = (TextView) convertView.findViewById(R.id.list_item);
        }
    }

}