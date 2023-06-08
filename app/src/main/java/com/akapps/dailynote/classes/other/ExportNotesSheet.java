package com.akapps.dailynote.classes.other;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.fragments.notes;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import org.jetbrains.annotations.NotNull;
import io.realm.Realm;
import io.realm.RealmResults;

public class ExportNotesSheet extends RoundedBottomSheetDialogFragment{

    private int numNotesSelected;
    private Fragment fragment;
    private boolean isOneNoteSelected;
    private Realm realm;
    private String noteText;

    public ExportNotesSheet(){}

    public ExportNotesSheet(int numNotesSelected, Fragment fragment, boolean isOneNoteSelected){
        this.numNotesSelected = numNotesSelected;
        this.fragment = fragment;
        this.isOneNoteSelected = isOneNoteSelected;
    }

    public ExportNotesSheet(int noteId, boolean isOneNoteSelected, String noteText){
        this.numNotesSelected = noteId;
        this.isOneNoteSelected = isOneNoteSelected;
        this.noteText = noteText;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_export_notes, container, false);

        TextView numNotesSelectedText = view.findViewById(R.id.number_of_notes);
        TextView title = view.findViewById(R.id.title);
        MaterialButton exportText = view.findViewById(R.id.export_text);
        MaterialButton exportMarkdown = view.findViewById(R.id.export_markdown);
        MaterialButton exportTextString = view.findViewById(R.id.export_text_string);
        MaterialButton exportTextStringFormatted = view.findViewById(R.id.export_text_string_formatted);
        MaterialButton exportNote = view.findViewById(R.id.export_note);
        MaterialButton info = view.findViewById(R.id.export_info);

        realm = RealmSingleton.getInstance(getContext());

        if (AppData.getAppData().isDarkerMode) {
            view.setBackgroundColor(getContext().getColor(R.color.darker_mode));
            exportText.setBackgroundColor(getContext().getColor(R.color.darker_mode));
            exportMarkdown.setBackgroundColor(getContext().getColor(R.color.darker_mode));
            exportTextString.setBackgroundColor(getContext().getColor(R.color.darker_mode));
            exportTextStringFormatted.setBackgroundColor(getContext().getColor(R.color.darker_mode));
            exportNote.setBackgroundColor(getContext().getColor(R.color.darker_mode));
            info.setBackgroundColor(getContext().getColor(R.color.not_too_dark_gray));
            exportText.setStrokeWidth(5);
            exportMarkdown.setStrokeWidth(5);
            exportTextString.setStrokeWidth(5);
            exportTextStringFormatted.setStrokeWidth(5);
            exportNote.setStrokeWidth(5);
            exportText.setStrokeColor(ColorStateList.valueOf(getContext().getColor(R.color.blue)));
            exportMarkdown.setStrokeColor(ColorStateList.valueOf(getContext().getColor(R.color.orange)));
            exportTextString.setStrokeColor(ColorStateList.valueOf(getContext().getColor(R.color.golden_rod)));
            exportTextStringFormatted.setStrokeColor(ColorStateList.valueOf(getContext().getColor(R.color.gumbo)));
            exportNote.setStrokeColor(ColorStateList.valueOf(getContext().getColor(R.color.money_green)));
        }
        else {
            view.setBackgroundColor(getContext().getColor(R.color.gray));
            exportTextString.setTextColor(getContext().getColor(R.color.gray));
        }

        if(!isOneNoteSelected){
            exportTextString.setVisibility(View.GONE);
            exportTextStringFormatted.setVisibility(View.GONE);
            exportNote.setVisibility(View.GONE);
            numNotesSelectedText.setText("" + numNotesSelected);
            info.setVisibility(View.GONE);
        }
        else
            title.setText("Export Note");

        exportText.setOnClickListener(view12 -> {
            if(isOneNoteSelected)
                exportNote(".txt");
            else
                exportNotes(".txt");
        });

        exportMarkdown.setOnClickListener(view1 -> {
            if(isOneNoteSelected)
                exportNote(".md");
            else
                exportNotes(".md");
        });

        exportTextString.setOnClickListener(view14 -> {
            String removeFormatting =  Helper.removeMarkdownFormatting(noteText);
            Helper.shareFile(getActivity(), removeFormatting);
            dismiss();
        });

        exportTextStringFormatted.setOnClickListener(view13 -> {
            Helper.shareFile(getActivity(), noteText);
            dismiss();
        });

        exportNote.setOnClickListener(view15 -> {
            Helper.shareNote(getActivity(), numNotesSelected, realm);
            dismiss();
        });

        info.setOnClickListener(view16 -> {

            String exportInfo = "" +
                    "• Text file\n" +
                    "  - export note as text file\n\n" +
                    "• Markdown file\n" +
                    "  - export note as markdown file\n\n" +
                    "• Text\n" +
                    "  - export note as text \n\n" +
                    "• Text (formatted)\n" +
                    "  - export note as markdown text\n\n" +
                    "• Export Note\n" +
                    "  - exports note as text\n" +
                    "  - exports all images and audio files";

            InfoSheet info1 = new InfoSheet(exportInfo, 12);
            info1.show(getActivity().getSupportFragmentManager(), info1.getTag());
        });

        return view;
    }

    private void exportNotes(String extension){
        RealmResults<Note> results = Helper.getSelectedNotes(((notes) fragment).realm,
                getActivity());
        if(results != null && results.size()!=0)
            Helper.exportFiles(extension, getActivity(), results , ((notes) fragment).realm);
        ((notes) fragment).clearMultipleSelect();
        dismiss();
    }

    private void exportNote(String extension){
        Helper.exportFiles(extension, getActivity(), realm.where(Note.class)
                .equalTo("noteId", numNotesSelected).findAll(), realm);
        dismiss();
    }

    @Override
    public int getTheme() {
        if(AppData.getAppData().isDarkerMode)
            return R.style.BaseBottomSheetDialogLight;
        else
            return R.style.BaseBottomSheetDialog;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    BottomSheetDialog dialog =(BottomSheetDialog) getDialog ();
                    if (dialog != null) {
                        FrameLayout bottomSheet = dialog.findViewById (R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            BottomSheetBehavior behavior = BottomSheetBehavior.from (bottomSheet);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                });
    }

}