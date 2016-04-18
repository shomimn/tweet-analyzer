package util;

import com.google.gson.JsonObject;

public interface OptionsHandler
{
    void changeUpdateInterval(String json);
    void changeTweetThreshold(String json);
    void changeTaxiThreshold(String json);

    JsonObject getOptions();
}
