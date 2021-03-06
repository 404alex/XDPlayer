package lxy.liying.hdtvneu.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.flyco.dialog.entity.DialogMenuItem;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.NormalListDialog;

import java.util.ArrayList;
import java.util.List;

import cc.duduhuo.applicationtoast.AppToast;
import lxy.liying.hdtvneu.R;
import lxy.liying.hdtvneu.activity.M3U8Player;
import lxy.liying.hdtvneu.activity.NEU_ReviewListActivity;
import lxy.liying.hdtvneu.app.App;
import lxy.liying.hdtvneu.domain.MarkGroup;
import lxy.liying.hdtvneu.domain.MarkItem;
import lxy.liying.hdtvneu.domain.Program;
import lxy.liying.hdtvneu.fragment.MarkIPv6Fragment;
import lxy.liying.hdtvneu.utils.Constants;

/**
 * =======================================================
 * 作者：liying
 * 日期：2016/5/15 15:41
 * 版本：1.0
 * 描述：节目列表适配器
 * 备注：
 * =======================================================
 */
public class NEU_ProgramsAdapter extends RecyclerView.Adapter<NEU_ProgramsAdapter.ViewHolder> {
    /** 节目列表集合 */
    private List<Program> programsList = new ArrayList<>(180);
    private Activity activity;
    private String queryString;     // 搜索的字符
    private ForegroundColorSpan foregroundColorSpan;
    private ArrayList<DialogMenuItem> mMenuItems = new ArrayList<>();

    /**
     * 构造方法
     */
    public NEU_ProgramsAdapter(Activity activity) {
        this.activity = activity;
        foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#148370"));
        mMenuItems.add(new DialogMenuItem("添加到收藏", R.drawable.ic_menu_mark));
        mMenuItems.add(new DialogMenuItem("复制播放地址", R.drawable.ic_menu_copy_link));
    }

    public void setData(List<Program> programs, String queryString) {
        this.queryString = queryString;
        if (programs != null) {
            programsList = programs;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_neu_program, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Program program = programsList.get(position);
        int index = TextUtils.isEmpty(queryString) ? -1 : program.getName().indexOf(queryString);
        if (index == -1) {
            holder.tvProgram.setText(program.getName());
        } else {
            SpannableString ss = new SpannableString(program.getName());
            ss.setSpan(foregroundColorSpan, index, index + queryString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvProgram.setText(ss);
        }

        holder.tvProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 进入播放界面
                Intent intent = new Intent(activity, M3U8Player.class);
                intent.putExtra(Constants.PROGRAM, program.getPath());
                activity.startActivity(intent);
            }
        });
        // 长按条目
        holder.tvProgram.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final NormalListDialog dialog = new NormalListDialog(activity, mMenuItems);
                dialog.title("请选择")//
                    .showAnim(App.mBasIn)//
                    .dismissAnim(App.mBasOut)//
                    .show();
                dialog.setOnOperItemClickL(new OnOperItemClickL() {
                    @Override
                    public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0:
                                // 收藏该节目
                                if (App.markService.getMarkByPath(program.getPath()) == null) {
                                    MarkItem item = new MarkItem(System.currentTimeMillis(), program.getName(), null,
                                        MarkGroup.NEU_TV, program.getPath(), null, 0L);
                                    App.markService.addMarkVideo(item);
                                    if (App.ipv6MarkItems != null) {
                                        App.ipv6MarkItems.add(item);
                                    }
                                    AppToast.showToast(program.getName() + " 已收藏。");
                                    MarkIPv6Fragment instance = MarkIPv6Fragment.getInstance();
                                    if (instance != null) {
                                        // 刷新MarkListFragment
                                        instance.markIPv6Adapter.addData(item);
                                        instance.markIPv6Adapter.notifyDataSetChanged();
                                        instance.tvMarkNone.setVisibility(View.GONE);
                                    }
                                } else {
                                    AppToast.showToast(program.getName() + " 已在收藏列表中。");
                                }
                                break;
                            case 1:
                                // 复制播放地址
                                ClipboardManager cmbName = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clipDataName = ClipData.newPlainText(null, program.getPath());
                                cmbName.setPrimaryClip(clipDataName);
                                AppToast.showToast("播放地址已复制。");
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                return true;
            }
        });
        holder.tvReview.setText("回看");
        holder.tvReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 进入回看列表选择
                Intent intent = new Intent(activity, NEU_ReviewListActivity.class);
                intent.putExtra(Constants.PROGRAM, new String[]{program.getName(), program.getP()});
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.push_left_in_ac, R.anim.push_left_out_ac);
            }
        });
    }

    @Override
    public int getItemCount() {
        return programsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvProgram;
        public TextView tvReview;

        public ViewHolder(View itemView) {
            super(itemView);
            tvProgram = (TextView) itemView.findViewById(R.id.tvProgram);
            tvReview = (TextView) itemView.findViewById(R.id.tvReview);
        }
    }
}
