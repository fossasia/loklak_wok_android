package org.loklak.wok.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.loklak.wok.model.search.Status;
import org.loklak.wok.model.search.User;
import org.loklak.wok.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchCategoryAdapter
        extends RecyclerView.Adapter<SearchCategoryAdapter.SearchViewHolder> {

    private Context mContext;
    private List<Status> mStatuses;

    public SearchCategoryAdapter(Context context, List<Status> statuses) {
        this.mContext = context;
        this.mStatuses = statuses;
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_tweet_search, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        holder.bind(mStatuses.get(position));
    }

    @Override
    public int getItemCount() {
        return mStatuses.size();
    }

    public ArrayList<Status> getStatuses() {
        return (ArrayList<Status>) mStatuses;
    }

    public void setStatuses(List<Status> statuses) {
        mStatuses = statuses;
        notifyDataSetChanged();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.user_profile_pic)
        ImageView userProfilePic;
        @BindView(R.id.user_fullname)
        TextView userFullname;
        @BindView(R.id.tweet_date)
        TextView tweetDate;
        @BindView(R.id.username)
        TextView username;
        @BindView(R.id.tweet_text)
        TextView tweetText;
        @BindView(R.id.tweet_photos)
        RecyclerView tweetPhotos;
        @BindView(R.id.number_retweets)
        TextView numberRetweets;
        @BindView(R.id.number_likes)
        TextView numberLikes;

        SearchViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Status status) {
            User user = status.getUser();

            Glide.with(mContext).load(user.getProfileImageUrlHttps()).into(userProfilePic);
            userFullname.setText(user.getName());
            username.setText(user.getScreenName());
            tweetText.setText(status.getText());
            numberRetweets.setText(String.valueOf(status.getRetweetCount()));
            numberLikes.setText(String.valueOf(status.getFavouritesCount()));

            List<String> imageUrls = filterImages(status.getImages());
            int orientation = StaggeredGridLayoutManager.VERTICAL;
            StaggeredGridLayoutManager layoutManager;
            if (imageUrls.size() > 0) {
                tweetPhotos.setVisibility(View.VISIBLE);
                if (imageUrls.size() == 1) {
                    layoutManager = new StaggeredGridLayoutManager(1, orientation);
                } else {
                    layoutManager = new StaggeredGridLayoutManager(2, orientation);
                }
                layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
                tweetPhotos.setLayoutManager(layoutManager);
                TweetImagesAdapter tweetImagesAdapter = new TweetImagesAdapter(mContext, imageUrls);
                tweetPhotos.setAdapter(tweetImagesAdapter);
            } else {
                tweetPhotos.setVisibility(View.GONE);
            }
        }

        private List<String> filterImages(List<String> imageUrls) {
            List<String> onlyPbsImages = new ArrayList<>();
            for (String url: imageUrls) {
                if (url.contains("pbs")) {
                    onlyPbsImages.add(url);
                }
            }
            return onlyPbsImages;
        }
    }
}
