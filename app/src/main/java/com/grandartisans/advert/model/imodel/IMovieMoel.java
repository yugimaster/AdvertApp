package com.grandartisans.advert.model.imodel;

import io.reactivex.Observable;

/**
 * author:  ljy
 * date:    2018/3/21
 * description:
 */

public interface IMovieMoel {
    Observable getPlayingMovie(int start, int count);

    Observable getCommingMovie(int start, int count);
}
