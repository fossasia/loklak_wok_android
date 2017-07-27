
package org.loklak.android.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.loklak.android.model.harvest.Status;
import org.loklak.android.model.harvest.User;
import org.loklak.android.wok.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HarvestedTweetAdapter
        extends RecyclerView.Adapter<HarvestedTweetAdapter.HarvestedTweetViewHolder> {

    private List<Status> mHarvestedTweetList;

    public HarvestedTweetAdapter(List<Status> harvestedTweets) {
        this.mHarvestedTweetList = harvestedTweets;
    }

    @Override
    public HarvestedTweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_harvested_tweet, parent, false);
        return new HarvestedTweetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HarvestedTweetViewHolder holder, int position) {
        holder.bind(mHarvestedTweetList.get(position));
    }

    @Override
    public int getItemCount() {
        return mHarvestedTweetList.size();
    }

    public void addHarvestedTweets(List<Status> harvestedTweetList) {
        int count = mHarvestedTweetList.size();
        mHarvestedTweetList.addAll(harvestedTweetList);
        notifyItemRangeInserted(getItemCount(), count);
    }

    public ArrayList<Status> getHarvestedTweetList() {
        return (ArrayList<Status>) mHarvestedTweetList;
    }

    public void clearAdapter() {
        mHarvestedTweetList.clear();
        notifyDataSetChanged();
    }

    class HarvestedTweetViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.user_fullname)
        TextView userFullname;
        @BindView(R.id.username)
        TextView username;
        @BindView(R.id.tweet_date)
        TextView tweetDate;
        @BindView(R.id.harvested_tweet_text)
        TextView harvestedTweetTextView;

        public HarvestedTweetViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Status harvestedTweet) {
            User user = harvestedTweet.getUser();
            userFullname.setText(user.getName());
            username.setText(user.getScreenName());
            tweetDate.setText(getReadableDate(harvestedTweet.getCreatedAt()));
            harvestedTweetTextView.setText(harvestedTweet.getText());
        }

        private String getReadableDate(Long miliseconds) {
            Date date = new Date(miliseconds);
            Locale locale = Locale.getDefault();
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(date);
            int day = gregorianCalendar.get(Calendar.DAY_OF_MONTH);
            String month = gregorianCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale);
            int year = gregorianCalendar.get(Calendar.YEAR);
            return String.valueOf(day) + " " + month + ", " + String.valueOf(year);
        }
    }
}
