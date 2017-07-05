package org.loklak.android;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utility {

    public static Gson getGsonForPrivateVariableClass() {
        return new GsonBuilder().setFieldNamingStrategy(field -> {
            String name = FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field);
            return name.substring(2); // private fields are named as mName i.e m_name
        }).create();
    }
}
