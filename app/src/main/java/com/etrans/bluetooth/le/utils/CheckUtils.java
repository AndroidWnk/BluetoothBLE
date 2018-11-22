package com.etrans.bluetooth.le.utils;

import android.content.Context;

import com.kaopiz.kprogresshud.KProgressHUD;

/**
 * Created by admin on 2017/8/29.
 */

public class CheckUtils {
    public static KProgressHUD showDialog(Context context) {
        KProgressHUD dialog = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f);
        return dialog;
    }
}
