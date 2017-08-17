package org.loklak.wok;


import android.content.Context;
import android.widget.Toast;

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

    public static void displayToast(Toast toast, Context context, String toastMessage) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT);
        toast.show();
    }
}
