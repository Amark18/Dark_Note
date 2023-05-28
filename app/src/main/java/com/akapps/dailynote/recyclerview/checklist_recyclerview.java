package com.akapps.dailynote.recyclerview;

import android.content.Context;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.other.ChecklistItemSheet;
import com.akapps.dailynote.classes.other.PlayAudioSheet;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stfalcon.imageviewer.StfalconImageViewer;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class checklist_recyclerview extends RecyclerView.Adapter<checklist_recyclerview.MyViewHolder>{

    // project data
    private final RealmResults<CheckListItem> checkList;
    private final Note currentNote;
    private Context context;
    private FragmentActivity activity;
    private final Realm realm;
    private User user;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView checklistText;
        private final TextView placeAttached;
        private final ImageView selectedIcon;
        private final ImageView deleteIcon;
        private final LinearLayout checkItem;
        private final LinearLayout edit;
        private final LinearLayout locationLayout;
        private final RecyclerView subChecklist;
        private final FloatingActionButton addSubChecklist;
        private final FloatingActionButton audio;
        private final MaterialCardView itemImageLayout;
        private final ImageView itemImage;
        private final MaterialCardView background;

        public MyViewHolder(View v) {
            super(v);
            checklistText = v.findViewById(R.id.note_Textview);
            placeAttached = v.findViewById(R.id.place_info);
            locationLayout = v.findViewById(R.id.location_layout);
            selectedIcon = v.findViewById(R.id.check_status);
            checkItem = v.findViewById(R.id.checkItem);
            edit = v.findViewById(R.id.edit);
            subChecklist = v.findViewById(R.id.subchecklist);
            addSubChecklist = v.findViewById(R.id.add_subchecklist);
            audio = v.findViewById(R.id.audio);
            subChecklist.setLayoutManager(new GridLayoutManager(v.getContext(), 1));
            itemImageLayout = v.findViewById(R.id.item_image_layout);
            itemImage = v.findViewById(R.id.item_image);
            deleteIcon = v.findViewById(R.id.delete_checklist_item);
            background = v.findViewById(R.id.background);
        }
    }

    public checklist_recyclerview(User user, RealmResults<CheckListItem> checkList, Note currentNote, Realm realm, FragmentActivity activity) {
        this.user = user;
        this.checkList = checkList;
        this.currentNote = currentNote;
        this.realm = realm;
        this.activity = activity;
    }

    @Override
    public checklist_recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_checklist_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        CheckListItem checkListItem = checkList.get(position);

        // search for duplicate sublist id
        int duplicateSize = realm.where(CheckListItem.class)
                .equalTo("subListId", checkListItem.getSubListId()).findAll().size();
        if(duplicateSize > 1){
            Random rand = new Random();
            realm.beginTransaction();
            checkListItem.setSubListId(rand.nextInt(100000) + 1);
            realm.commitTransaction();
        }

        if(checkListItem.getPlace() != null && !checkListItem.getPlace().getPlaceName().isEmpty()){
            holder.locationLayout.setVisibility(View.VISIBLE);
            holder.placeAttached.setText(checkListItem.getPlace().getPlaceName());
        }
        else
            holder.locationLayout.setVisibility(View.GONE);

        boolean recordingExists = false;
        if(checkListItem.getAudioPath() != null)
            recordingExists = new File(checkListItem.getAudioPath()).length() > 0;

        if(null != checkListItem.getAudioPath() && !checkListItem.getAudioPath().isEmpty() && recordingExists) {
            holder.audio.setVisibility(View.VISIBLE);
            holder.selectedIcon.setVisibility(View.GONE);
        }
        else {
            holder.audio.setVisibility(View.GONE);
            holder.selectedIcon.setVisibility(View.VISIBLE);

            if(null != checkListItem.getAudioPath() && !checkListItem.getAudioPath().isEmpty()){
                realm.beginTransaction();
                checkListItem.setAudioPath("");
                checkListItem.setAudioDuration(0);
                realm.commitTransaction();
            }
        }

        if(user.isEnableDeleteIcon())
            holder.deleteIcon.setVisibility(View.VISIBLE);
        else
            holder.deleteIcon.setVisibility(View.GONE);

        if(user.isModeSettings()){
            holder.background.setCardBackgroundColor(activity.getColor(R.color.darker_mode));
            holder.background.setStrokeColor(activity.getColor(R.color.light_gray));
            holder.background.setStrokeWidth(5);
        }

        holder.subChecklist.setAdapter(null);
        RecyclerView.Adapter subChecklistAdapter = null;
        if(user.isEnableSublists() && currentNote.isEnableSublist()) {
            holder.addSubChecklist.setVisibility(View.VISIBLE);
            if (null == checkListItem.getSubChecklist()) {
                realm.beginTransaction();
                checkListItem.setSubChecklist(new RealmList<>());
                realm.commitTransaction();
                holder.subChecklist.setVisibility(View.GONE);
            }
            else {
                if(checkListItem.getSubChecklist().size() != 0) {
                    holder.subChecklist.setVisibility(View.VISIBLE);
                    subChecklistAdapter = new sub_checklist_recyclerview(realm.where(SubCheckListItem.class)
                            .equalTo("id", checkListItem.getSubListId())
                            .sort("positionInList").findAll(), currentNote, realm, activity);
                    holder.subChecklist.setAdapter(subChecklistAdapter);
                    subChecklistAdapter.notifyItemChanged(position);
                }
            }
        }
        else{
            holder.subChecklist.setVisibility(View.GONE);
            holder.addSubChecklist.setVisibility(View.GONE);
        }


        // checks to see if there is a reminder and makes sure it has not passed
        if (!currentNote.getReminderDateTime().isEmpty()) {
            Date reminderDate = null;
            try {
                reminderDate = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse(currentNote.getReminderDateTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date now = new Date();
            if (now.after(reminderDate)) {
                realm.beginTransaction();
                currentNote.setReminderDateTime("");
                realm.commitTransaction();
            }
        }

        // retrieves checkList text and select status of checkListItem
        String checkListText = checkListItem.getText();
        boolean isSelected = checkListItem.isChecked();

        // populates note into the recyclerview
        holder.checklistText.setText(recordingExists && checkListItem.getText().isEmpty() ? "[Audio]" : checkListText);

        String textSize = Helper.getPreference(context, "size");
        if(textSize==null)
            textSize = "20";
        holder.checklistText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Integer.parseInt(textSize));

        // if note is selected, then it shows a strike through the text, changes the icon
        // to be filled and changes text color to gray
        if(isSelected) {
            holder.checklistText.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            holder.checklistText.setTextColor(Helper.darkenColor(currentNote.getTextColor(), 100));
            holder.selectedIcon.setImageDrawable(context.getDrawable(R.drawable.checked_icon));
        }
        else {
            holder.checklistText.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            holder.selectedIcon.setImageDrawable(context.getDrawable(R.drawable.unchecked_icon));
            holder.checklistText.setTextColor(currentNote.getTextColor());
        }

        // show if checklist item has an image
        if(checkListItem.getItemImage()!=null && !checkListItem.getItemImage().isEmpty()) {
            holder.itemImageLayout.setVisibility(View.VISIBLE);
            Glide.with(context).load(checkListItem.getItemImage()).into(holder.itemImage);
        }
        else
            holder.itemImageLayout.setVisibility(View.GONE);

        holder.audio.setOnClickListener(view -> {
            if(holder.audio.getVisibility() == View.VISIBLE) {
                PlayAudioSheet playAudioSheet = new PlayAudioSheet(checkListItem);
                playAudioSheet.show(activity.getSupportFragmentManager(), playAudioSheet.getTag());
            }
        });

        RecyclerView.Adapter finalSubChecklistAdapter = subChecklistAdapter;
        holder.addSubChecklist.setOnClickListener(view -> {
            ChecklistItemSheet checklistItemSheet = new ChecklistItemSheet(checkListItem, checkListText, true, finalSubChecklistAdapter);
            checklistItemSheet.show(activity.getSupportFragmentManager(), checklistItemSheet.getTag());
        });

        // if checklist item is clicked, then it updates the status of the item
        holder.checkItem.setOnClickListener(v -> {
            saveSelected(checkListItem, !isSelected);
            isAllItemsSelected();
            if(currentNote.getSort() == 3 || currentNote.getSort() == 4)
                notifyDataSetChanged();
            else
                notifyItemChanged(position);
        });

        holder.edit.setOnClickListener(v -> {
            openEditDialog(checkListItem, position);
        });

        holder.itemImageLayout.setOnClickListener(view -> {
            ArrayList<String> images = new ArrayList<>();
            images.add(checkListItem.getItemImage());
            new StfalconImageViewer.Builder<>(context, images, (imageView, image) ->
                    Glide.with(context).load(image).into(imageView))
                    .withBackgroundColor(context.getColor(R.color.gray))
                    .allowZooming(true)
                    .allowSwipeToDismiss(true)
                    .withHiddenStatusBar(false)
                    .withStartPosition(0)
                    .withTransitionFrom(holder.itemImage)
                    .show();
        });

        holder.deleteIcon.setOnClickListener(view -> {
            RealmHelper.deleteChecklistItem(checkListItem, false);
            realm.beginTransaction();
            currentNote.setDateEdited(new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime()));
            realm.commitTransaction();
            ((NoteEdit) activity).updateDateEdited();
            notifyDataSetChanged();
        });

        holder.locationLayout.setOnClickListener(view -> Helper.openMapView(activity, checkListItem.getPlace()));
    }

    @Override
    public int getItemCount() {
        return checkList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    // updates select status of note in database
    private void saveSelected(CheckListItem checkListItem, boolean status){
        // save status to database
        realm.beginTransaction();
        checkListItem.setChecked(status);

        if(null != checkListItem.getSubChecklist()) {
            if (checkListItem.getSubChecklist().size() > 0) {
                RealmResults<SubCheckListItem> subCheckListItems = realm.where(SubCheckListItem.class).equalTo("id", checkListItem.getSubListId()).findAll();
                subCheckListItems.setBoolean("checked", status);
            }
        }

        if(status)
            checkListItem.setLastCheckedDate(Helper.dateToCalender(Helper.getCurrentDate()).getTimeInMillis());
        else
            checkListItem.setLastCheckedDate(0);
        realm.commitTransaction();

        ((NoteEdit)context).updateSaveDateEdited();
    }

    // opens dialog that allows user to edit or delete checklist item
    private void openEditDialog(CheckListItem checkListItem, int position){
        ChecklistItemSheet checklistItemSheet = new ChecklistItemSheet(checkListItem, position, this);
        checklistItemSheet.show(activity.getSupportFragmentManager(), checklistItemSheet.getTag());
    }

    // determines if all items are select and if they are, checklist is set to check or "finished"
    private void isAllItemsSelected(){
        RealmResults<CheckListItem> select = checkList.where()
                .equalTo("checked", true)
                .findAll();

        boolean isAllChecked = select.size() == checkList.size();
        if(select.size()==0 && checkList.size()==0)
            isAllChecked = false;

        if(currentNote.isChecked() != isAllChecked) {
            realm.beginTransaction();
            currentNote.setChecked(isAllChecked);
            realm.commitTransaction();

            if(currentNote.isChecked())
                ((NoteEdit)context).title.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            else
                ((NoteEdit)context).title.setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        }
    }
}
