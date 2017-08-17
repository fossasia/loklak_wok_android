package org.loklak.wok.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.loklak.wok.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TweetImagesAdapter extends
        RecyclerView.Adapter<TweetImagesAdapter.TweetImagesViewHolder> {

    private Context mContext;
    private List<String> mImageUrls;

    public TweetImagesAdapter(Context context, List<String> imageUrls) {
        this.mContext = context;
        this.mImageUrls = imageUrls;
    }

    @Override
    public TweetImagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.grid_elem_tweet_photo, parent, false);
        return new TweetImagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TweetImagesViewHolder holder, int position) {
        holder.bind(mImageUrls.get(position));
    }

    @Override
    public int getItemCount() {
        return mImageUrls.size();
    }

    class TweetImagesViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tweet_image)
        ImageView tweetImage;

        public TweetImagesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(String imageUrl) {
            Glide.with(mContext).load(imageUrl).fitCenter().into(tweetImage);
        }
    }
}
