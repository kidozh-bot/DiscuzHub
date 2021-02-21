package com.kidozh.discuzhub.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ThreadActivity;
import com.kidozh.discuzhub.activities.UserProfileActivity;
import com.kidozh.discuzhub.daos.ViewHistoryDao;
import com.kidozh.discuzhub.database.ViewHistoryDatabase;
import com.kidozh.discuzhub.entities.Thread;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.User;
import com.kidozh.discuzhub.utilities.AnimationUtils;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;
import com.kidozh.discuzhub.utilities.VibrateUtils;
import com.kidozh.discuzhub.utilities.ConstUtils;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.NetworkUtils;
import com.kidozh.discuzhub.utilities.numberFormatUtils;
import com.kidozh.discuzhub.utilities.TimeDisplayUtils;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = ThreadAdapter.class.getSimpleName();
    public List<Thread> threadList = new ArrayList<>();
    public boolean ignoreDigestStyle = false;
    Context mContext;
    public String fid;
    Discuz bbsInfo;
    User userBriefInfo;
    Map<String,String> threadType;
    ViewHistoryDao viewHistoryDao;

    public ThreadAdapter(Map<String,String> threadType, String fid, Discuz bbsInfo, User userBriefInfo){
        this.bbsInfo = bbsInfo;
        this.userBriefInfo = userBriefInfo;

        this.threadType = threadType;
        this.fid = fid;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        if(threadList !=null){
            return (long) threadList.get(position).tid;
        }
        return position;
    }

    public void clearList(){
        int oldSize = this.threadList.size();
        this.threadList.clear();
        notifyItemRangeRemoved(0,oldSize);
    }

    public void addThreadInfoList(@NonNull List<Thread> threadList, Map<String,String> threadType){
        this.threadType = threadType;
        int oldSize = this.threadList.size();
        this.threadList.addAll(threadList);
        Log.d(TAG,"Insert to thread adapter starting at "+oldSize+" count "+ threadList.size());

        notifyItemRangeInserted(oldSize, threadList.size());
    }

    @Override
    public int getItemViewType(int position) {
        Thread thread = threadList.get(position);
        if(thread.displayOrder <= 0){
            return R.layout.item_thread;
        }
        else {
            return R.layout.item_thread_pinned;
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        mContext = context;
        viewHistoryDao = ViewHistoryDatabase.getInstance(mContext).getDao();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        if(!ignoreDigestStyle && viewType == R.layout.item_thread_pinned){
            View view = inflater.inflate(R.layout.item_thread_pinned, parent, shouldAttachToParentImmediately);
            return new PinnedViewHolder(view);
        }
        else {
            // normal item
            if(!UserPreferenceUtils.conciseRecyclerView(context)){
                View view = inflater.inflate(R.layout.item_thread, parent, shouldAttachToParentImmediately);
                return new ThreadViewHolder(view);
            }
            else {
                View view = inflater.inflate(R.layout.item_thread_concise, parent, shouldAttachToParentImmediately);
                return new ConciseThreadViewHolder(view);
            }
        }



    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderRaw, int position) {
        Thread thread = threadList.get(position);
        if(thread == null ){
            return;
        }
        if(holderRaw instanceof PinnedViewHolder){
            PinnedViewHolder holder = (PinnedViewHolder) holderRaw;
            Spanned sp = Html.fromHtml(thread.subject);
            SpannableString spannableString = new SpannableString(sp);
            holder.mTitle.setText(spannableString, TextView.BufferType.SPANNABLE);

            // thread type
            if(thread.displayOrder !=0){
                int textResource = R.string.bbs_forum_pinned;
                switch(thread.displayOrder){
                    case 3:
                        textResource = R.string.display_order_3;
                        break;
                    case 2:
                        textResource = R.string.display_order_2;
                        break;
                    case 1:
                        textResource = R.string.display_order_1;
                        break;
                    case -1:
                        textResource = R.string.display_order_n1;
                        break;
                    case -2:
                        textResource = R.string.display_order_n2;
                        break;
                    case -3:
                        textResource = R.string.display_order_n3;
                        break;
                    case -4:
                        textResource = R.string.display_order_n4;
                        break;
                    default:
                        textResource = R.string.bbs_forum_pinned;
                }
                holder.mThreadType.setText(textResource);
                //holder.mThreadType.setBackgroundColor(mContext.getColor(R.color.colorAccent));
            }
            else {
                if(threadType == null){
                    holder.mThreadType.setVisibility(View.GONE);

                }
                else {
                    // provided by label
                    holder.mThreadType.setVisibility(View.VISIBLE);
                    String type = threadType.get(String.valueOf(thread.typeId));

                    if(type !=null){
                        Spanned threadSpanned = Html.fromHtml(type);
                        SpannableString threadSpannableString = new SpannableString(threadSpanned);
                        holder.mThreadType.setText(threadSpannableString);
                    }
                    else {
                        holder.mThreadType.setText(R.string.bbs_forum_pinned);
                    }

                }


            }
            holder.mCardview.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ThreadActivity.class);
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                    intent.putExtra(ConstUtils.PASS_THREAD_KEY, thread);
                    intent.putExtra("FID", thread.fid);
                    intent.putExtra("TID", thread.tid);
                    intent.putExtra("SUBJECT", thread.subject);
                    VibrateUtils.vibrateForClick(mContext);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) mContext,
                            Pair.create(holder.mTitle, "bbs_thread_subject")
                    );

                    Bundle bundle = options.toBundle();
                    mContext.startActivity(intent,bundle);
                }
            });
        }
        else if(holderRaw instanceof ThreadViewHolder){
            ThreadViewHolder holder = (ThreadViewHolder) holderRaw;
            Spanned sp = Html.fromHtml(thread.subject);
            SpannableString spannableString = new SpannableString(sp);
            holder.mTitle.setText(spannableString, TextView.BufferType.SPANNABLE);
            holder.mThreadViewNum.setText(numberFormatUtils.getShortNumberText(thread.views));
            holder.mThreadReplyNum.setText(numberFormatUtils.getShortNumberText(thread.replies));

            holder.mPublishDate.setText(TimeDisplayUtils.getLocalePastTimeString(mContext, thread.publishAt));

            if(thread.displayOrder !=0){
                int textResource = R.string.bbs_forum_pinned;
                switch(thread.displayOrder){
                    case 3:
                        textResource = R.string.display_order_3;
                        break;
                    case 2:
                        textResource = R.string.display_order_2;
                        break;
                    case 1:
                        textResource = R.string.display_order_1;
                        break;
                    case -1:
                        textResource = R.string.display_order_n1;
                        break;
                    case -2:
                        textResource = R.string.display_order_n2;
                        break;
                    case -3:
                        textResource = R.string.display_order_n3;
                        break;
                    case -4:
                        textResource = R.string.display_order_n4;
                        break;
                    default:
                        textResource = R.string.bbs_forum_pinned;
                }
                holder.mThreadType.setText(textResource);
                //holder.mThreadType.setBackgroundColor(mContext.getColor(R.color.colorAccent));
            }
            else {
                if(threadType == null){
                    holder.mThreadType.setVisibility(View.GONE);

                }
                else {
                    // provided by label
                    holder.mThreadType.setVisibility(View.VISIBLE);
                    String type = threadType.get(String.valueOf(thread.typeId));

                    if(type !=null){
                        Spanned threadSpanned = Html.fromHtml(type);
                        SpannableString threadSpannableString = new SpannableString(threadSpanned);
                        holder.mThreadType.setText(threadSpannableString);
                    }
                    else {
                        holder.mThreadType.setText(String.format("%s",position+1));
                    }

                }

                //holder.mThreadType.setBackgroundColor(mContext.getColor(R.color.ThreadTypeBackgroundColor));
            }

            holder.mThreadPublisher.setText(thread.author);

            int avatar_num = thread.authorId % 16;
            if(avatar_num < 0){
                avatar_num = -avatar_num;
            }

            int avatarResource = mContext.getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",mContext.getPackageName());

            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(mContext));
            Glide.get(mContext).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
            String source = URLUtils.getSmallAvatarUrlByUid(thread.authorId);
            RequestOptions options = new RequestOptions()
                    .placeholder(mContext.getDrawable(avatarResource))
                    .error(mContext.getDrawable(avatarResource))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)

                    .priority(Priority.HIGH);
            GlideUrl glideUrl = new GlideUrl(source,
                    new LazyHeaders.Builder().addHeader("referer",bbsInfo.base_url).build()
            );

            if(NetworkUtils.canDownloadImageOrFile(mContext)){
                Glide.with(mContext)
                        .load(glideUrl)
                        .apply(options)
                        .into(holder.mAvatarImageview);
            }
            else {
                Glide.with(mContext)
                        .load(glideUrl)
                        .apply(options)
                        .onlyRetrieveFromCache(true)
                        .into(holder.mAvatarImageview);
            }
            // set short reply
            if(thread.recommendNum !=0){
                holder.mRecommendationNumber.setVisibility(View.VISIBLE);
                holder.mRecommendationNumber.setText(numberFormatUtils.getShortNumberText(thread.recommendNum));

            }
            else {
                holder.mRecommendationNumber.setVisibility(View.GONE);

            }

            if(thread.readPerm == 0){
                holder.mReadPerm.setVisibility(View.GONE);

            }
            else {

                holder.mReadPerm.setVisibility(View.VISIBLE);
                holder.mReadPerm.setText(String.valueOf(thread.readPerm));
                //holder.mReadPerm.setText(numberFormatUtils.getShortNumberText(threadInfo.readPerm));
                int readPermissionVal = thread.readPerm;
                if(userBriefInfo == null || userBriefInfo.readPerm < readPermissionVal){
                    holder.mReadPerm.setTextColor(mContext.getColor(R.color.colorWarn));
                }
                else {
                    holder.mReadPerm.setTextColor(mContext.getColor(R.color.colorTextDefault));
                }
            }

            if(thread.attachment == 0){
                holder.mAttachmentIcon.setVisibility(View.GONE);
            }
            else {
                holder.mAttachmentIcon.setVisibility(View.VISIBLE);
                if(thread.attachment == 1){
                    holder.mAttachmentIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_thread_attachment_24px));
                }
                else {
                    holder.mAttachmentIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_image_outlined_24px));
                }
            }

            if(thread.price !=0 ){

                holder.mPriceNumber.setText(String.valueOf(thread.price));
                holder.mPriceNumber.setVisibility(View.VISIBLE);
            }
            else {
                // holder.mPriceNumber.setText(String.valueOf(threadInfo.price));
                holder.mPriceNumber.setVisibility(View.GONE);
            }


            if(thread.shortReplyList!=null && thread.shortReplyList.size()>0){
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
                holder.mReplyRecyclerview.setFocusable(false);
                holder.mReplyRecyclerview.setNestedScrollingEnabled(false);
                holder.mReplyRecyclerview.setLayoutManager(linearLayoutManager);
                holder.mReplyRecyclerview.setClickable(false);
                ShortPostAdapter adapter = new ShortPostAdapter();
                adapter.setShortReplyInfoList(thread.shortReplyList);
                holder.mReplyRecyclerview.setItemAnimator(AnimationUtils.INSTANCE.getRecyclerviewAnimation(mContext));
                holder.mReplyRecyclerview.setAdapter(adapter);
                holder.mReplyRecyclerview.setNestedScrollingEnabled(false);
            }
            else {

            }

            holder.mCardview.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ThreadActivity.class);
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                    intent.putExtra(ConstUtils.PASS_THREAD_KEY, thread);
                    intent.putExtra("FID", thread.fid);
                    intent.putExtra("TID", thread.tid);
                    intent.putExtra("SUBJECT", thread.subject);
                    VibrateUtils.vibrateForClick(mContext);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) mContext,
                            Pair.create(holder.mTitle, "bbs_thread_subject")
                    );

                    Bundle bundle = options.toBundle();
                    mContext.startActivity(intent,bundle);
                }
            });

            holder.mAvatarImageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, UserProfileActivity.class);
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                    intent.putExtra("UID", thread.authorId);

                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation((Activity) mContext, holder.mAvatarImageview, "user_info_avatar");

                    Bundle bundle = options.toBundle();

                    mContext.startActivity(intent,bundle);
                }
            });
        }
        else if(holderRaw instanceof ConciseThreadViewHolder){
            ConciseThreadViewHolder holder = (ConciseThreadViewHolder) holderRaw;

            Spanned sp = Html.fromHtml(thread.subject);
            SpannableString spannableString = new SpannableString(sp);
            holder.mTitle.setText(spannableString, TextView.BufferType.SPANNABLE);

            holder.mThreadReplyNum.setText(numberFormatUtils.getShortNumberText(thread.replies));

            holder.mPublishDate.setText(TimeDisplayUtils.getLocalePastTimeString(mContext, thread.publishAt));

            //holder.mPublishDate.setText(df.format(threadInfo.publishAt));
            if(thread.displayOrder !=0){
                int textResource = R.string.bbs_forum_pinned;
                switch(thread.displayOrder){
                    case 3:
                        textResource = R.string.display_order_3;
                        break;
                    case 2:
                        textResource = R.string.display_order_2;
                        break;
                    case 1:
                        textResource = R.string.display_order_1;
                        break;
                    case -1:
                        textResource = R.string.display_order_n1;
                        break;
                    case -2:
                        textResource = R.string.display_order_n2;
                        break;
                    case -3:
                        textResource = R.string.display_order_n3;
                        break;
                    case -4:
                        textResource = R.string.display_order_n4;
                        break;
                    default:
                        textResource = R.string.bbs_forum_pinned;
                }
                holder.mThreadType.setText(textResource);
                holder.mThreadType.setTextColor(mContext.getColor(R.color.colorAccent));
                holder.mThreadType.setVisibility(View.VISIBLE);
            }
            else {
                holder.mThreadType.setVisibility(View.GONE);

            }

            int avatar_num = thread.authorId % 16;
            if(avatar_num < 0){
                avatar_num = -avatar_num;
            }

            int avatarResource = mContext.getResources().getIdentifier(String.format("avatar_%s",avatar_num+1),"drawable",mContext.getPackageName());

            OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(NetworkUtils.getPreferredClient(mContext));
            Glide.get(mContext).getRegistry().replace(GlideUrl.class, InputStream.class,factory);
            String source = URLUtils.getDefaultAvatarUrlByUid(thread.authorId);
            RequestOptions options = new RequestOptions()
                    .placeholder(mContext.getDrawable(avatarResource))
                    .error(mContext.getDrawable(avatarResource));
            GlideUrl glideUrl = new GlideUrl(source,
                    new LazyHeaders.Builder().addHeader("referer",bbsInfo.base_url).build()
            );

            if(NetworkUtils.canDownloadImageOrFile(mContext)){
                Glide.with(mContext)
                        .load(glideUrl)
                        .apply(options)
                        .into(holder.mAvatarImageview);
            }
            else {
                Glide.with(mContext)
                        .load(glideUrl)
                        .apply(options)
                        .onlyRetrieveFromCache(true)
                        .into(holder.mAvatarImageview);
            }


            holder.mThreadPublisher.setText(thread.author);
            holder.mCardview.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ThreadActivity.class);
                    intent.putExtra(ConstUtils.PASS_BBS_ENTITY_KEY,bbsInfo);
                    intent.putExtra(ConstUtils.PASS_BBS_USER_KEY,userBriefInfo);
                    intent.putExtra(ConstUtils.PASS_THREAD_KEY, thread);
                    intent.putExtra("FID", thread.fid);
                    intent.putExtra("TID", thread.tid);
                    intent.putExtra("SUBJECT", thread.subject);
                    VibrateUtils.vibrateForClick(mContext);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) mContext,
                            Pair.create(holder.mTitle, "bbs_thread_subject")
                    );

                    Bundle bundle = options.toBundle();
                    mContext.startActivity(intent,bundle);
                }
            });


        }


    }

    @Override
    public int getItemCount() {
        if(threadList == null){
            return 0;
        }
        else {
            return threadList.size();
        }

    }
    
    public class PinnedViewHolder extends RecyclerView.ViewHolder{
        
        TextView mTitle;
        TextView mThreadType;
        CardView mCardview;
        public PinnedViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.bbs_thread_title);
            mThreadType = itemView.findViewById(R.id.bbs_thread_type);
            mCardview = itemView.findViewById(R.id.bbs_thread_cardview);
           
        }
    }

    public class ThreadViewHolder extends RecyclerView.ViewHolder{
        
        TextView mThreadPublisher;
        TextView mPublishDate;
        TextView mTitle;
        TextView mContent;
        TextView mThreadViewNum;
        TextView mThreadReplyNum;
        TextView mThreadType;
        ShapeableImageView mAvatarImageview;
        CardView mCardview;
        RecyclerView mReplyRecyclerview;
        TextView mRecommendationNumber;
        TextView mReadPerm;
        ImageView mAttachmentIcon;
        TextView mPriceNumber;
        public ThreadViewHolder(@NonNull View itemView) {
            super(itemView);
            mThreadPublisher = itemView.findViewById(R.id.bbs_post_publisher);
            mPublishDate = itemView.findViewById(R.id.bbs_post_publish_date);
            mTitle = itemView.findViewById(R.id.bbs_thread_title);
            mContent = itemView.findViewById(R.id.bbs_thread_content);
            mThreadViewNum = itemView.findViewById(R.id.bbs_thread_view_textview);
            mThreadReplyNum = itemView.findViewById(R.id.bbs_thread_reply_number);
            mThreadType = itemView.findViewById(R.id.bbs_thread_type);
            mAvatarImageview = itemView.findViewById(R.id.bbs_post_avatar_imageView);
            mCardview = itemView.findViewById(R.id.bbs_thread_cardview);
            mReplyRecyclerview = itemView.findViewById(R.id.bbs_thread_short_reply_recyclerview);
            mRecommendationNumber = itemView.findViewById(R.id.bbs_thread_recommend_number);
            mReadPerm = itemView.findViewById(R.id.bbs_thread_read_perm_number);
            mAttachmentIcon = itemView.findViewById(R.id.bbs_thread_attachment_image);
            mPriceNumber = itemView.findViewById(R.id.bbs_thread_price_number);
        }
    }

    public class ConciseThreadViewHolder extends RecyclerView.ViewHolder{
        TextView mThreadPublisher;
        TextView mPublishDate;
        TextView mTitle;
        TextView mThreadReplyNum;
        TextView mThreadType;
        CardView mCardview;
        ShapeableImageView mAvatarImageview;



        public ConciseThreadViewHolder(@NonNull View itemView) {
            super(itemView);
            mThreadPublisher = itemView.findViewById(R.id.bbs_post_publisher);
            mPublishDate = itemView.findViewById(R.id.bbs_post_publish_date);
            mTitle = itemView.findViewById(R.id.bbs_thread_title);
            mThreadReplyNum = itemView.findViewById(R.id.bbs_thread_reply_number);
            mThreadType = itemView.findViewById(R.id.bbs_thread_type);
            mAvatarImageview = itemView.findViewById(R.id.bbs_post_avatar_imageView);
            mCardview = itemView.findViewById(R.id.bbs_thread_cardview);
        }
    }
}
