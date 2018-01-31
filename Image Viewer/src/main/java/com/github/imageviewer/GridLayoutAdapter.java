package com.github.imageviewer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.imageviewer.helper.FlickrImage;
import com.github.imageviewer.helper.OnItemClickListener;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by AT-Praveen on 30/01/18.
 */

public class GridLayoutAdapter extends RecyclerView.Adapter<GridLayoutAdapter.ViewHolder> {
    private List<FlickrImage> mList;
    private Context mContext;
    private OnItemClickListener mItemClickListener;

    GridLayoutAdapter(Context context, List<FlickrImage> flickrImageList, OnItemClickListener listener) {
        mContext = context;
        mList = flickrImageList;
        mItemClickListener = listener;
    }

    public void setImageList(List<FlickrImage> list) {
        this.mList = list;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_itemview, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemClickListener.onItemClick(view, viewHolder.getAdapterPosition());
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FlickrImage flickrImage = mList.get(position);
        holder.mArticleTitle.setText(flickrImage.getTitle());

        Picasso.with(mContext)
                .load(flickrImage.getFlickrPhotoURI())
                .into(holder.mArticleImage);
    }

    @Override
    public int getItemCount() {
        if (mList != null) {
            return mList.size();
        } else {
            return 0;
        }
    }

     class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mArticleImage;
        private TextView mArticleTitle;

         ViewHolder(View itemView) {
            super(itemView);

            mArticleImage = itemView.findViewById(R.id.article_image);
            mArticleTitle = itemView.findViewById(R.id.article_title);
        }
    }
}
