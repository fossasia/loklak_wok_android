package org.loklak.android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.loklak.android.wok.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class PostTweetMediaAdapter
        extends RecyclerView.Adapter<PostTweetMediaAdapter.PostTweetMediaViewHolder> {

    private final String LOG_TAG = PostTweetMediaAdapter.class.getName();

    private Context mContext;
    private List<Bitmap> mBitmapList = new ArrayList<>();

    public PostTweetMediaAdapter(Context context, List<Bitmap> files) {
        this.mContext = context;
        this.mBitmapList = files;
    }

    @Override
    public PostTweetMediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_post_tweet_image, parent, false);
        return new PostTweetMediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostTweetMediaViewHolder holder, int position) {
        holder.bind(mBitmapList.get(position));
    }

    @Override
    public int getItemCount() {
        return mBitmapList.size();
    }

    public List<Bitmap> getBitmapList() {
        return mBitmapList;
    }

    public void setBitmapList(List<Bitmap> bitmaps) {
        this.mBitmapList = bitmaps;
        notifyDataSetChanged();
    }

    public void addBitmap(Bitmap bitmap) {
        mBitmapList.add(bitmap);
        notifyDataSetChanged();
    }

    public void clearAdapter() {
        mBitmapList.clear();
        notifyDataSetChanged();
    }

    class PostTweetMediaViewHolder extends RecyclerView.ViewHolder {

        private final String LOG_TAG = PostTweetMediaViewHolder.class.getName();

        @BindView(R.id.tweet_media)
        ImageView tweetMedia;
        @BindView(R.id.tweet_media_remove)
        ImageButton tweetMediaRemove;

        public PostTweetMediaViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Bitmap bitmap) {
            tweetMedia.setImageBitmap(bitmap);
        }

        @OnClick(R.id.tweet_media_remove)
        public void onClickTweetMediaRemove() {
            int position = getAdapterPosition();
            mBitmapList.remove(position);
            notifyItemRemoved(position);
        }
    }
}
