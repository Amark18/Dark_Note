package com.akapps.dailynote.classes.other;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

public class GenericInfoSheet extends RoundedBottomSheetDialogFragment {

    private String titleText;
    private String message;
    private String proceedText = "";
    private String cancelText = "";
    private int action;

    public GenericInfoSheet() {
    }

    public GenericInfoSheet(String title, String message) {
        this.titleText = title;
        this.message = message;
    }

    public GenericInfoSheet(String title, String message, String proceedText, String cancelText) {
        this.titleText = title;
        this.message = message;
        this.proceedText = proceedText;
        this.cancelText = cancelText;
    }

    public GenericInfoSheet(String title, String message, String proceedText, int action) {
        this.titleText = title;
        this.message = message;
        this.proceedText = proceedText;
        this.action = action;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_generic_info, container, false);

        TextView title = view.findViewById(R.id.title);
        TextView info = view.findViewById(R.id.info);
        MaterialButton cancel = view.findViewById(R.id.cancel);
        MaterialButton proceed = view.findViewById(R.id.proceed);

        title.setText(titleText);
        proceed.setText(proceedText.isEmpty() ? proceed.getText() : proceedText);
        cancel.setText(cancelText.isEmpty() ? cancel.getText() : cancelText);
        info.setText(message);
        info.setGravity(Gravity.CENTER);

        cancel.setOnClickListener(v -> this.dismiss());

        proceed.setOnClickListener(view1 -> {
            if(action == 1) openAppSettings();
            this.dismiss();
        });

        return view;
    }

    private void openAppSettings(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
        startActivity(intent);
    }

    @Override
    public int getTheme() {
        return UiHelper.getBottomSheetTheme(getContext());
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        UiHelper.setBottomSheetBehavior(view, dialog);
    }

}