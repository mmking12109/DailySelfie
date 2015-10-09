package com.example.yang.dailyselfie;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class SelfieAdapter extends BaseAdapter{

    private ArrayList<Selfie> list = new ArrayList<Selfie>();
    private static LayoutInflater inflater;
    private Context mContext;

    public SelfieAdapter(Context context) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
    }

    public int getCount(){
        return list.size();
    }

    public Object getItem(int position){
        return list.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View newView = convertView;
        ViewHolder holder;

        final Selfie curr = list.get(position);

        if (null == convertView) {
            holder = new ViewHolder();
            newView = inflater
                    .inflate(R.layout.new_selfie, parent, false);
            holder.photo = (ImageView) newView.findViewById(R.id.photo);
            holder.title = (TextView) newView.findViewById(R.id.title);
            newView.setTag(holder);

        } else {
            holder = (ViewHolder) newView.getTag();
        }

        holder.photo.setImageBitmap(curr.getPhoto());
        // Set the date as the text
        holder.title.setText(curr.getDate());

        // onItemClickListener moved here because nothing works in xml
        newView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String photoPath = curr.getPath();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + photoPath), "image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });

        return newView;
    }

    static class ViewHolder {

        ImageView photo;
        TextView title;

    }

    public void add(Selfie listItem) {
        list.add(listItem);
        notifyDataSetChanged();
    }

    public void delete(int position){
        list.remove(position);
        notifyDataSetChanged();
    }

    public ArrayList<Selfie> getList() {
        return list;
    }

    public void removeAllViews() {
        list.clear();
        this.notifyDataSetChanged();
    }


}
