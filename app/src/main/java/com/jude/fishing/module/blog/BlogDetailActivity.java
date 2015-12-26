package com.jude.fishing.module.blog;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jude.beam.bijection.RequiresPresenter;
import com.jude.beam.expansion.list.BeamListActivity;
import com.jude.beam.expansion.list.ListConfig;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.exgridview.ExGridView;
import com.jude.fishing.R;
import com.jude.fishing.model.AccountModel;
import com.jude.fishing.model.BlogModel;
import com.jude.fishing.model.entities.PersonBrief;
import com.jude.fishing.model.entities.SeedComment;
import com.jude.fishing.model.entities.SeedDetail;
import com.jude.fishing.model.service.ServiceResponse;
import com.jude.fishing.module.user.LoginActivity;
import com.jude.fishing.module.user.UserDetailActivity;
import com.jude.fishing.utils.RecentDateFormat;
import com.jude.fishing.widget.NetImageAdapter;
import com.jude.tagview.TAGView;
import com.jude.utils.JTimeTransform;
import com.jude.utils.JUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zhuchenxi on 15/9/27.
 */
@RequiresPresenter(BlogDetailPresenter.class)
public class BlogDetailActivity extends BeamListActivity<BlogDetailPresenter, SeedComment> {


    @InjectView(R.id.avatar)
    SimpleDraweeView avatar;
    @InjectView(R.id.name)
    TextView name;
    @InjectView(R.id.time)
    TextView time;
    @InjectView(R.id.content)
    TextView content;
    @InjectView(R.id.pictures)
    ExGridView pictures;
    @InjectView(R.id.address)
    TextView address;
    @InjectView(R.id.praise_image)
    ImageView praiseImage;
    @InjectView(R.id.praise_count)
    TextView praiseCount;
    @InjectView(R.id.btn_praise)
    LinearLayout btnPraise;
    @InjectView(R.id.comment_image)
    ImageView commentImage;
    @InjectView(R.id.comment_count)
    TextView commentCount;
    @InjectView(R.id.btn_comment)
    LinearLayout btnComment;
    @InjectView(R.id.tool)
    LinearLayout tool;
    @InjectView(R.id.praise_member)
    ExGridView praiseMember;
    @InjectView(R.id.praise_text)
    TextView praiseText;
    @InjectView(R.id.praise_container)
    RelativeLayout praiseContainer;
    @InjectView(R.id.author_container)
    RelativeLayout authorContainer;

    EditText reply;
    TAGView replySend;

    public View getBlogDetailView(SeedDetail data, ViewGroup parent) {
        View view = getLayoutInflater().inflate(R.layout.blog_item_head, parent, false);
        ButterKnife.inject(this, view);
        avatar.setImageURI(Uri.parse(data.getAuthorAvatar()));
        authorContainer.setOnClickListener(v -> {
            Intent i = new Intent(this, UserDetailActivity.class);
            i.putExtra("id", data.getAuthorId());
            startActivity(i);
        });
        name.setText(data.getAuthorName());
        time.setText(new JTimeTransform(data.getTime()).toString(new RecentDateFormat()));
        content.setText(data.getContent());
        address.setText(data.getAddress());
        btnPraise.setOnClickListener(v -> getPresenter().blogPraise(data.getPraiseStatus()));
        praiseImage.setImageResource(data.getPraiseStatus() ? R.drawable.ic_collect_red : R.drawable.ic_collect_gray);
        praiseCount.setText(data.getPraiseCount() + "");
        commentCount.setText(data.getCommentCount() + "");
        btnComment.setOnClickListener(v -> showCommentEdit(0, data.getAuthorName()));
        praiseContainer.setVisibility(data.getPraiseCount() == 0 ? View.GONE : View.VISIBLE);

        if (data.getImages() != null && data.getImages().length != 0) {
            pictures.setAdapter(new NetImageAdapter(parent.getContext(), data.getImages()));
            pictures.setColumnCount(Math.min(data.getImages().length, 3));
        }

        for (PersonBrief personBrief : data.getPraiseMember()) {
            SimpleDraweeView draweeView = new SimpleDraweeView(this);
            draweeView.setLayoutParams(new ViewGroup.LayoutParams(JUtils.dip2px(40), JUtils.dip2px(40)));
            draweeView.setImageURI(Uri.parse(personBrief.getAvatar()));
            draweeView.getHierarchy().setRoundingParams(RoundingParams.asCircle());
            draweeView.setOnClickListener(v -> {
                Intent i = new Intent(this, UserDetailActivity.class);
                i.putExtra("id", personBrief.getUID());
                startActivity(i);
            });
            praiseMember.addView(draweeView);
        }
        reply.setHint("输入回复");
        replySend.setOnClickListener(v->{
            if (reply.toString().trim().isEmpty()) {
                JUtils.Toast("回复不能为空");
                return;
            }
            getPresenter().sentComment(0, reply.getText().toString());
            reply.setText("");
        });

        return view;
    }

    @Override
    protected BaseViewHolder getViewHolder(ViewGroup viewGroup, int i) {
        return new SeedCommentViewHolder(viewGroup, this);
    }

    public void showCommentEdit(int fid, String fname) {
        if (AccountModel.getInstance().getAccount() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        reply.setHint("输入对" + fname + "的回复");
        replySend.setOnClickListener(v->{
            if (reply.toString().trim().isEmpty()) {
                JUtils.Toast("回复不能为空");
                return;
            }
            getPresenter().sentComment(fid, reply.getText().toString());
        });
    }

    @Override
    protected ListConfig getConfig() {
        return super.getConfig().setRefreshAble(true);
    }

    @Override
    public int getLayout() {
        return R.layout.blog_activity_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reply = $(R.id.reply);
        replySend = $(R.id.reply_send);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_seed,menu);
        if (AccountModel.getInstance().getAccount()!=null&&AccountModel.getInstance().getAccount().getUID()==getPresenter().wid){
            menu.findItem(R.id.report).setVisible(false);
        }else{
            menu.findItem(R.id.delete).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.report){
            new MaterialDialog.Builder(this)
                    .title("举报")
                    .content("你确定要举报这条渔获吗?")
                    .input("请写下您的理由", "", (materialDialog, charSequence) -> {
                        BlogModel.getInstance().reportBlog(getPresenter().wid,charSequence.toString()).subscribe(new ServiceResponse<Object>() {
                            @Override
                            public void onNext(Object o) {
                                JUtils.Toast("感谢您的举报");
                            }
                        });
                    })
                    .negativeText("取消")
                    .positiveColor(Color.RED)
                    .positiveText("举报")
                    .show();
        }
        if (item.getItemId() == R.id.delete){
            new MaterialDialog.Builder(this)
                    .title("确定要删除本条渔获吗?")
                    .content("删除后将不可恢复")
                    .negativeText("取消")
                    .positiveColor(Color.RED)
                    .positiveText("删除")
                    .onPositive((materialDialog, dialogAction) -> BlogModel.getInstance().deleteBlog(getPresenter().wid).subscribe(new ServiceResponse<Object>() {
                        @Override
                        public void onNext(Object o) {
                            JUtils.Toast("已删除,请手动刷新");
                        }
                    }))
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}
