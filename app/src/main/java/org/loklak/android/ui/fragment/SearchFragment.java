package org.loklak.android.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import org.loklak.android.adapters.SearchFragmentPagerAdapter;
import org.loklak.android.ui.activity.SuggestActivity;
import org.loklak.android.utility.Constants;
import org.loklak.android.wok.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SearchFragment extends Fragment {

    private final String LOG_TAG = SearchFragment.class.getName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tweet_search_edit_text)
    EditText tweetSearchEditText;
    @BindView(R.id.clear_image_button)
    ImageButton clearImageButton;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.view_pager)
    ViewPager viewPager;

    private String mQuery;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, rootView);

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
        toolbar.setNavigationOnClickListener(view -> getActivity().onBackPressed());

        Intent intent = getActivity().getIntent();
        mQuery = intent.getStringExtra(Constants.TWEET_SEARCH_SUGGESTION_QUERY_KEY);
        tweetSearchEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                Intent suggestIntent = new Intent(getActivity(), SuggestActivity.class);
                startActivity(suggestIntent);
            }
        });

        tabLayout.setupWithViewPager(viewPager);
        setupViewPager(viewPager);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        tweetSearchEditText.setText(mQuery);
    }

    private void setupViewPager(ViewPager viewPager) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        SearchFragmentPagerAdapter pagerAdapter = new SearchFragmentPagerAdapter(fragmentManager);
        pagerAdapter.addFragment(SearchCategoryFragment.newInstance("", mQuery), "LATEST");
        pagerAdapter.addFragment(SearchCategoryFragment.newInstance("image", mQuery), "PHOTOS");
        pagerAdapter.addFragment(SearchCategoryFragment.newInstance("video", mQuery), "VIDEOS");
        viewPager.setAdapter(pagerAdapter);
    }

    @OnClick(R.id.clear_image_button)
    public void setOnClickClearImageButtonListener(View view) {
        tweetSearchEditText.setText("");
        tweetSearchEditText.clearFocus();
    }
}
