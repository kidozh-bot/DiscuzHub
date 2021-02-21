package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.PublishActivity;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.ThreadDraft;
import com.kidozh.discuzhub.entities.User;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.TimeDisplayUtils;

import java.util.ArrayList;
import java.util.List;



public class ThreadDraftAdapter extends RecyclerView.Adapter<ThreadDraftAdapter.ViewHolder> {

    List<ThreadDraft> threadDraftList = new ArrayList<>();
    Context context;
    private User userBriefInfo;
    Discuz bbsInfo;

    public ThreadDraftAdapter(Discuz bbsInfo, User userBriefInfo){
        this.userBriefInfo = userBriefInfo;
        this.bbsInfo = bbsInfo;
    }

    public void setThreadDraftList(@NonNull List<ThreadDraft> threadDraftList) {
        int oldSize = this.threadDraftList.size();
        this.threadDraftList.clear();
        notifyItemRangeRemoved(0,oldSize);
        this.threadDraftList.addAll(threadDraftList);
        notifyItemRangeInserted(0, threadDraftList.size());
    }

    public List<ThreadDraft> getThreadDraftList() {
        return threadDraftList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.item_bbs_thread_draft;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThreadDraft threadDraft = threadDraftList.get(position);
        if(threadDraft.typeName.isEmpty()){
            holder.bbsThreadDraftType.setVisibility(View.GONE);
        }
        else {
            holder.bbsThreadDraftType.setVisibility(View.VISIBLE);
            holder.bbsThreadDraftType.setText(threadDraft.typeName);
        }
        holder.bbsThreadDraftMessage.setText(threadDraft.content);
        holder.bbsThreadDraftType.setText(threadDraft.typeName);
        if(threadDraft.subject.isEmpty()){
            holder.bbsThreadDraftSubject.setText(R.string.bbs_not_set);
        }
        else {
            holder.bbsThreadDraftSubject.setText(threadDraft.subject);
        }

        holder.bbsThreadDraftForum.setText(threadDraft.forumName);
        holder.bbsThreadDraftUpdateTime.setText(TimeDisplayUtils.getLocalePastTimeString(context,threadDraft.lastUpdateAt));
        holder.bbsThreadCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PublishActivity.class);
                intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY, bbsInfo);
                intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                intent.putExtra(ConstUtils.PASS_THREAD_DRAFT_KEY,threadDraft);
                intent.putExtra("fid",threadDraft.fid);
                intent.putExtra("fid_name",threadDraft.forumName);
                intent.putExtra(ConstUtils.PASS_POST_TYPE, ConstUtils.TYPE_POST_DRAFT);
                VibrateUtils.vibrateForClick(context);
                context.startActivity(intent);
            }
        });
        if(threadDraft.password == null || threadDraft.password.length() == 0){
            holder.bbsThreadDraftPasswordIcon.setVisibility(View.GONE);
        }
        else {
            holder.bbsThreadDraftPasswordIcon.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public int getItemCount() {
        if(threadDraftList == null){
            return 0;
        }
        else {
            return threadDraftList.size();
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        
        TextView bbsThreadDraftType;
        TextView bbsThreadDraftSubject;
        TextView bbsThreadDraftMessage;
        TextView bbsThreadDraftUpdateTime;
        TextView bbsThreadDraftForum;
        CardView bbsThreadCardView;
        ImageView bbsThreadDraftPasswordIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bbsThreadDraftType = itemView.findViewById(R.id.bbs_thread_draft_type);
            bbsThreadDraftSubject = itemView.findViewById(R.id.bbs_thread_draft_subject);
            bbsThreadDraftMessage = itemView.findViewById(R.id.bbs_thread_draft_message);
            bbsThreadDraftUpdateTime = itemView.findViewById(R.id.bbs_thread_draft_update_time);
            bbsThreadDraftForum = itemView.findViewById(R.id.bbs_thread_draft_forum);
            bbsThreadCardView = itemView.findViewById(R.id.bbs_thread_draft_cardview);
            bbsThreadDraftPasswordIcon = itemView.findViewById(R.id.bbs_thread_draft_password_icon);
        }
    }
}
