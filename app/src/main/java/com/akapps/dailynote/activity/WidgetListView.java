package com.akapps.dailynote.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import java.util.ArrayList;
import java.util.List;

public class WidgetListView extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int noteId = intent.getIntExtra("id", 0);
        return new WidgetListViewFactory(getApplicationContext(), noteId, AppData.getNoteChecklist(noteId, getApplicationContext()));
    }

    class WidgetListViewFactory implements RemoteViewsService.RemoteViewsFactory {
        private final Context context;
        private List<String> checklist;
        private int noteId;

        public WidgetListViewFactory(Context context, int noteId, ArrayList<String> checklist) {
            this.context = context;
            this.noteId = noteId;
            this.checklist = checklist;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            new Handler(Looper.getMainLooper()).post(() -> checklist = AppData.getNoteChecklist(noteId, context));
        }

        @Override
        public void onDestroy() {
            checklist.clear();
        }

        @Override
        public int getCount() {
            return checklist.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            String currentItem = checklist.get(position);
            RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.recyclerview_widget);

            if(!currentItem.contains("-Note-")) {
                remoteView.setViewVisibility(R.id.widget_check_status, View.VISIBLE);

                if (currentItem.contains("~~")) {
                    remoteView.setInt(R.id.checklist_text, "setPaintFlags", Paint.STRIKE_THRU_TEXT_FLAG);
                    remoteView.setTextColor(R.id.checklist_text, getColor(R.color.gray_300));
                    remoteView.setImageViewResource(R.id.widget_check_status, R.drawable.checked_icon);
                } else {
                    remoteView.setInt(R.id.checklist_text, "setPaintFlags", 0);
                    remoteView.setTextColor(R.id.checklist_text, getColor(R.color.white));
                    remoteView.setImageViewResource(R.id.widget_check_status, R.drawable.unchecked_icon);
                }

                if(currentItem.contains("⤷")) {
                    currentItem = currentItem.replace("⤷", "");
                    remoteView.setTextColor(R.id.checklist_text, Helper.darkenColor(getColor(R.color.white), 200));
                    remoteView.setViewVisibility(R.id.sublist_spacing, View.VISIBLE);
                }
                else
                    remoteView.setViewVisibility(R.id.sublist_spacing, View.GONE);

                if(currentItem.contains("♬")){
                    currentItem = currentItem.equals("♬") ? "[Audio]" : currentItem.replace("♬", "");
                    remoteView.setViewVisibility(R.id.widget_check_status, View.GONE);
                    remoteView.setViewVisibility(R.id.audio, View.VISIBLE);
                }
                else {
                    remoteView.setViewVisibility(R.id.audio, View.GONE);
                    remoteView.setViewVisibility(R.id.widget_check_status, View.VISIBLE);
                }
            }
            else {
                remoteView.setViewVisibility(R.id.widget_check_status, View.GONE);
                remoteView.setViewVisibility(R.id.audio, View.GONE);
            }

            String checklistItemText = currentItem.replace("~~", "").replace("-Note-", "");
            checklistItemText += checklistItemText.isEmpty() ? "[Audio]": "";

            remoteView.setTextViewText(R.id.checklist_text, Html.fromHtml(checklistItemText, Html.FROM_HTML_MODE_COMPACT));

            Intent fillInIntent = new Intent();
            remoteView.setOnClickFillInIntent(R.id.checklist_text, fillInIntent);

            return remoteView;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
