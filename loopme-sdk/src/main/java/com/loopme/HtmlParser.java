package com.loopme;


import com.loopme.common.StaticParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by katerina on 2/6/17.
 */

public class HtmlParser {

    private static final String ADID = "ADID";
    private static final String CAMP_NAME = "CAMP_NAME";
    private static final String LI_NAME = "LI_NAME";
    private static final String CREATIVEID = "CREATIVEID";
    private static final String APP_NAME = "APP_NAME";
    private static final String ADVERTISER = "ADVERTISER";

    private static final String MACROS = "macros";
    private static final String EMPTY_STRING = "";
    private String mHtml;
    private String mJsonScript;

    public HtmlParser() {
    }

    public HtmlParser(String html) {
        this.mHtml = html;
        mJsonScript = mHtml.substring(mHtml.indexOf("{"), mHtml.lastIndexOf("}"));
    }

    public String getObject(String name) {
        try {
            JSONObject jsonObject = new JSONObject(mJsonScript);
            String param = jsonObject.getJSONObject(MACROS).getString(name);
            String decodedParam = decode(param);
            return decodedParam;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return EMPTY_STRING;
    }

    @Deprecated
    public static String decode(String source) {
        try {
            return URLDecoder.decode(source, StaticParams.UTF_8);
        } catch (UnsupportedEncodingException var2) {
            return URLDecoder.decode(source);
        }
    }
    public String getAdvertiserId() {
        return getObject(ADVERTISER);
    }

    public String getCampaignId() {
        return getObject(CAMP_NAME);
    }

    public String getLineItemId() {
        return getObject(LI_NAME);
    }

    public String getCreativeId() {
        return getObject(CREATIVEID);
    }

    public String getAppId() {
        return getObject(APP_NAME);
    }

    public String getPlacementId() {
        return EMPTY_STRING;
    }
}
