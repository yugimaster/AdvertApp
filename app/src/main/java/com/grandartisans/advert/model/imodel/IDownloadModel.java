package com.grandartisans.advert.model.imodel;

import io.reactivex.Observable;

/**
 * author:  ljy
 * date:    2018/3/23
 * description:
 */

public interface IDownloadModel {

    Observable downloadFile(String url);

}
