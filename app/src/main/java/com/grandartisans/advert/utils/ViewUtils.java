package com.grandartisans.advert.utils;

import android.content.res.Resources;
import android.widget.ImageView;

import com.grandartisans.advert.R;

/**
 * Created by yugimaster on 2019/4/16.
 */
public class ViewUtils {

    public static void setWeatherIcon(Resources resources, ImageView imageView, Long weatherType) {
        if (weatherType == 0) {
            imageView.setImageDrawable(resources.getDrawable(R.drawable.heavy_snow));
        } else if (weatherType == 1) {
            imageView.setImageDrawable(resources.getDrawable(R.drawable.heavy_rain));
        } else if (weatherType == 2) {
            imageView.setImageDrawable(resources.getDrawable(R.drawable.thunder_rain));
        } else if (weatherType == 3) {
            imageView.setImageDrawable(resources.getDrawable(R.drawable.dust));
        } else if (weatherType == 4) {
            imageView.setImageDrawable(resources.getDrawable(R.drawable.sunny));
        } else if (weatherType == 5) {
            imageView.setImageDrawable(resources.getDrawable(R.drawable.sand_storm));
        } else if (weatherType == 6) {
            imageView.setImageDrawable(resources.getDrawable(R.drawable.fog));
        } else if (weatherType == 7) {
            imageView.setImageDrawable(resources.getDrawable(R.drawable.light_rain));
        } else if (weatherType == 8) {
            imageView.setImageDrawable(resources.getDrawable(R.drawable.shade));
        } else if (weatherType == 9) {
            imageView.setImageDrawable(resources.getDrawable(R.drawable.rain_snow));
        } else if (weatherType == 10) {
            imageView.setImageDrawable(resources.getDrawable(R.drawable.cloud));
        } else if (weatherType == 11) {
            imageView.setImageDrawable(resources.getDrawable(R.drawable.rain));
        }
    }
}
