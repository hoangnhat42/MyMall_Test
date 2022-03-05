package com.project.scan_on;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomePageAdapter extends RecyclerView.Adapter {

    private List<HomePageModel> homePageModelList;
    private RecyclerView.RecycledViewPool recycledViewPool;
    private int lastPosition = -1;

    public HomePageAdapter(List<HomePageModel> homePageModelList) {
        this.homePageModelList = homePageModelList;
        recycledViewPool = new RecyclerView.RecycledViewPool();
    }

    @Override
    public int getItemViewType(int position) {
        switch (homePageModelList.get(position).getType()) {

            case 0:
                return HomePageModel.BANNER_SLIDER;
            case 1:
                return HomePageModel.STRIP_AD_BANNER;
            case 2:
                return HomePageModel.HORIZONTAL_PRODUCT_VIEW;
            case 3:
                return HomePageModel.GRID_PRODUCT_VIEW;


            default:
                return -1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        switch (viewType) {
            case HomePageModel.BANNER_SLIDER:
                View bannerSliderView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sliding_ad_layout, viewGroup, false);
                return new BannerSliderViewholder(bannerSliderView);
            case HomePageModel.STRIP_AD_BANNER:
                View stripAdView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.strip_ad_layout, viewGroup, false);
                return new stripAdBannerViewholder(stripAdView);
            case HomePageModel.HORIZONTAL_PRODUCT_VIEW:
                View horizontalProductView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.horizontal_scroll_layout, viewGroup, false);
                return new HorizontalProductViewholder(horizontalProductView);
            case HomePageModel.GRID_PRODUCT_VIEW:
                View gridProductView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.grid_product_layout, viewGroup, false);
                return new GridProductViewholder(gridProductView);

            default:
                return null;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        switch (homePageModelList.get(position).getType()) {

            case HomePageModel.BANNER_SLIDER:
                List<SliderModel> sliderModelList = homePageModelList.get(position).getSliderModelList();
                ((BannerSliderViewholder) viewHolder).setBannersliderViewPager(sliderModelList);
                break;

            case HomePageModel.STRIP_AD_BANNER:
                String resource = homePageModelList.get(position).getResource();
                String color = homePageModelList.get(position).getBackgroundColor();
                ((stripAdBannerViewholder) viewHolder).setStripAd(resource, color);
                break;
            case HomePageModel.HORIZONTAL_PRODUCT_VIEW:
                String layoutColor = homePageModelList.get(position).getBackgroundColor();
                String horizontalLayoutTitle = homePageModelList.get(position).getTitle();
                List<WishlistModel> viewAllProductList = homePageModelList.get(position).getViewAllProductList();
                List<HorizontalProductScrollModel> horizontalProductScrollModelList = homePageModelList.get(position).getHorizontalProductScrollModelList();
                ((HorizontalProductViewholder) viewHolder).setHorizontalProductLayout(horizontalProductScrollModelList, horizontalLayoutTitle, layoutColor, viewAllProductList);
                break;
            case HomePageModel.GRID_PRODUCT_VIEW:
                String gridLayoutColor = homePageModelList.get(position).getBackgroundColor();
                String gridLayoutTitle = homePageModelList.get(position).getTitle();
                List<HorizontalProductScrollModel> gridProductScrollModelList = homePageModelList.get(position).getHorizontalProductScrollModelList();
                ((GridProductViewholder) viewHolder).setGridProductLayout(gridProductScrollModelList, gridLayoutTitle, gridLayoutColor);
            default:
                return;
        }

        if (lastPosition < position) {
            Animation animation = AnimationUtils.loadAnimation(viewHolder.itemView.getContext(), R.anim.fade_in);
            viewHolder.itemView.setAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return homePageModelList.size();
    }


    public class BannerSliderViewholder extends RecyclerView.ViewHolder {
        private ViewPager bannersliderViewPager;
        private int currentPage;
        private Timer timer;
        final private long DELAY_TIME = 3000;
        final private long PERIOD_TIME = 3000;
        private List <SliderModel> arrangedList;


        public BannerSliderViewholder(@NonNull View itemView) {
            super(itemView);

            bannersliderViewPager = itemView.findViewById(R.id.banner_slider_view_pager);
        }

        private void setBannersliderViewPager(final List<SliderModel> sliderModelList) {
             currentPage = 2;
            if (timer != null){
                timer.cancel();
            }
            arrangedList = new ArrayList<>();
            for (int x = 0;x < sliderModelList.size();x++){

                arrangedList.add(x,sliderModelList.get(x));
            }
            arrangedList.add(0,sliderModelList.get(sliderModelList.size() -2));
            arrangedList.add(1,sliderModelList.get(sliderModelList.size() -1));
            arrangedList.add(sliderModelList.get(0));
            arrangedList.add(sliderModelList.get(1));

            SliderAdapter sliderAdapter = new SliderAdapter(arrangedList);
            bannersliderViewPager.setAdapter(sliderAdapter);
            bannersliderViewPager.setClipToPadding(false);
            bannersliderViewPager.setPageMargin(20);

            bannersliderViewPager.setCurrentItem(currentPage);


            ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int il) {

                }

                @Override
                public void onPageSelected(int i) {
                    currentPage = i;
                }

                @Override
                public void onPageScrollStateChanged(int i) {
                    if (i == ViewPager.SCROLL_STATE_IDLE) {
                        pageLooper(arrangedList);
                    }
                }
            };
            bannersliderViewPager.addOnPageChangeListener(onPageChangeListener);

            startBannerSlideshow(arrangedList);

            bannersliderViewPager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    pageLooper(arrangedList);
                    stopBannerSlideShow();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        startBannerSlideshow(arrangedList);
                    }
                    return false;
                }
            });
        }

        private void pageLooper(List<SliderModel> sliderModelList) {
            if (currentPage == sliderModelList.size() - 2) {
                currentPage = 2;
                bannersliderViewPager.setCurrentItem(currentPage, false);
            }
            if (currentPage == 1) {
                currentPage = sliderModelList.size() - 3;
                bannersliderViewPager.setCurrentItem(currentPage, false);
            }
        }

        private void startBannerSlideshow(final List<SliderModel> sliderModelList) {
            final Handler handler = new Handler();
            final Runnable update = new Runnable() {
                @Override
                public void run() {
                    if (currentPage >= sliderModelList.size()) {
                        currentPage = 1;
                    }
                    bannersliderViewPager.setCurrentItem(currentPage++, true);
                }
            };
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(update);
                }
            }, DELAY_TIME, PERIOD_TIME);

        }

        private void stopBannerSlideShow() {
            timer.cancel();
        }
    }

    public class stripAdBannerViewholder extends RecyclerView.ViewHolder {

        private ImageView stripAdImage;
        private ConstraintLayout stripAdContainer;


        public stripAdBannerViewholder(@NonNull View itemView) {
            super(itemView);
            stripAdImage = itemView.findViewById(R.id.strip_ad_image);
            stripAdContainer = itemView.findViewById(R.id.strip_ad_container);
        }

        private void setStripAd(String resource, String color) {
            Glide.with(itemView.getContext()).load(resource).apply(new RequestOptions().placeholder(R.mipmap.placeholder_big)).into(stripAdImage);
            stripAdContainer.setBackgroundColor(Color.parseColor(color));
        }
    }

    public class HorizontalProductViewholder extends RecyclerView.ViewHolder {

        private ConstraintLayout container;
        private TextView horizontalLayoutTitle;
        private Button horizontalLayoutViewAllBtn;
        private RecyclerView horizontalRecyclerView;

        public HorizontalProductViewholder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            horizontalLayoutTitle = itemView.findViewById(R.id.horizontal_scroll_layout_title);
            horizontalLayoutViewAllBtn = itemView.findViewById(R.id.horizontal_scroll_view_all_btn);
            horizontalRecyclerView = itemView.findViewById(R.id.horizontal_scroll_layout_recyclerview);
            horizontalRecyclerView.setRecycledViewPool(recycledViewPool);
        }
        private void setHorizontalProductLayout(List<HorizontalProductScrollModel> horizontalProductScrollModelList, final String title, String color, final List<WishlistModel> viewAllProductList){
              container.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
             horizontalLayoutTitle.setText(title);
             if (horizontalProductScrollModelList.size() > 8){
                 horizontalLayoutViewAllBtn.setVisibility(View.VISIBLE);

                 horizontalLayoutViewAllBtn.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         ViewAllActivity.wishlistModelList = viewAllProductList;
                         Intent viewAllIntent = new Intent(itemView.getContext(),ViewAllActivity.class);
                         viewAllIntent.putExtra("layout_code",0);
                         viewAllIntent.putExtra("title",title);
                         itemView.getContext().startActivity(viewAllIntent);
                     }
                 });
             }else {
                 horizontalLayoutViewAllBtn.setVisibility(View.INVISIBLE);
             }

            HorizontalProductScrollAdapter horizontalProductScrollAdapter = new HorizontalProductScrollAdapter(horizontalProductScrollModelList);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(itemView.getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            horizontalRecyclerView.setLayoutManager(linearLayoutManager);

            horizontalRecyclerView.setAdapter(horizontalProductScrollAdapter);
            horizontalProductScrollAdapter.notifyDataSetChanged();
        }


    }

    public class GridProductViewholder extends RecyclerView.ViewHolder {

        private ConstraintLayout container;
        private TextView gridLayoutTitle;
        private Button gridLayoutViewAllBtn;
        private GridLayout gridProductLayout;

        public GridProductViewholder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            gridLayoutTitle = itemView.findViewById(R.id.grid_product_layout_title);
            gridLayoutViewAllBtn = itemView.findViewById(R.id.grid_product_layout_viewall_btn);
            gridProductLayout = itemView.findViewById(R.id.grid_layout);
        }
        private void setGridProductLayout(final List<HorizontalProductScrollModel> horizontalProductScrollModelList, final String title, String color){
            container.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
            gridLayoutTitle.setText(title);


           for (int x = 0;x < 4;x++){
               ImageView productImage = gridProductLayout.getChildAt(x).findViewById(R.id.h_s_product_image);
               TextView productTitle = gridProductLayout.getChildAt(x).findViewById(R.id.h_s_product_title);
               TextView productDiscription = gridProductLayout.getChildAt(x).findViewById(R.id.h_s_product_description);
               TextView productPrice = gridProductLayout.getChildAt(x).findViewById(R.id.h_s_product_price);

               Glide.with(itemView.getContext()).load(horizontalProductScrollModelList.get(x).getProduceImage()).apply(new RequestOptions().placeholder(R.mipmap.placehlder_small)).into(productImage);
               productTitle.setText(horizontalProductScrollModelList.get(x).getProductTitle());
               productDiscription.setText(horizontalProductScrollModelList.get(x).getProductDescription());
               productPrice.setText("Rs."+horizontalProductScrollModelList.get(x).getProductPrice()+"/-");
               gridProductLayout.getChildAt(x).setBackgroundColor(Color.parseColor("#FFFFFF"));


               if (!title.equals("")) {
                   final int finalX = x;
                   gridProductLayout.getChildAt(x).setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           Intent productDetailsIntent = new Intent(itemView.getContext(), ProductDetailsActivity.class);
                           productDetailsIntent.putExtra("PRODUCT_ID",horizontalProductScrollModelList.get(finalX).getProductID());
                           itemView.getContext().startActivity(productDetailsIntent);
                       }
                   });
               }

           }
            if (!title.equals("")) {
                gridLayoutViewAllBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewAllActivity.horizontalProductScrollModelList = horizontalProductScrollModelList;
                        Intent viewAllIntent = new Intent(itemView.getContext(), ViewAllActivity.class);
                        viewAllIntent.putExtra("layout_code", 1);
                        viewAllIntent.putExtra("title", title);
                        itemView.getContext().startActivity(viewAllIntent);
                    }
                });
            }
        }
    }

}
