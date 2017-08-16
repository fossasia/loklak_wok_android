package org.loklak.wok.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.loklak.wok.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class PostTweetMediaAdapter
        extends RecyclerView.Adapter<PostTweetMediaAdapter.PostTweetMediaViewHolder> {

    private final String LOG_TAG = PostTweetMediaAdapter.class.getName();

    private Context mContext;
    private List<String> mImagePathList = new ArrayList<>();

    public PostTweetMediaAdapter(Context context, List<String> imagePaths) {
        this.mContext = context;
        this.mImagePathList = imagePaths;
    }

    @Override
    public PostTweetMediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_post_tweet_image, parent, false);
        return new PostTweetMediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostTweetMediaViewHolder holder, int position) {
        holder.bind(mImagePathList.get(position));
    }

    @Override
    public int getItemCount() {
        return mImagePathList.size();
    }

    public ArrayList<String> getImagePathList() {
        return (ArrayList<String>) mImagePathList;
    }

    public void setImagePathList(List<String> imagePaths) {
        this.mImagePathList = imagePaths;
        notifyDataSetChanged();
    }

    public void addImagePath(String imagePath) {
        mImagePathList.add(imagePath);
        notifyDataSetChanged();
    }

    public void clearAdapter() {
        mImagePathList.clear();
        notifyDataSetChanged();
    }

    class PostTweetMediaViewHolder extends RecyclerView.ViewHolder {

        private final String LOG_TAG = PostTweetMediaViewHolder.class.getName();

        @BindView(R.id.tweet_media)
        ImageView tweetMedia;
        @BindView(R.id.tweet_media_remove)
        ImageButton tweetMediaRemove;

        PostTweetMediaViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(String imagePath) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            tweetMedia.setImageBitmap(bitmap);
        }

        @OnClick(R.id.tweet_media_remove)
        public void onClickTweetMediaRemove() {
            int position = getAdapterPosition();
            mImagePathList.remove(position);
            notifyItemRemoved(position);
        }
    }
}
