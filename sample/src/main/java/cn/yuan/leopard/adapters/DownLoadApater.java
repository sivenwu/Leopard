package cn.yuan.leopard.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yuan.leopardkit.LeopardHttp;
import com.yuan.leopardkit.download.DownLoadManager;
import com.yuan.leopardkit.download.model.DownloadInfo;
import com.yuan.leopardkit.interfaces.IProgress;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.yuan.leopard.R;
import cn.yuan.leopard.model.DownLoadModel;

/**
 * Created by Yuan on 2016/9/1.
 * Detail 下载适配器
 */
public class DownLoadApater extends RecyclerView.Adapter<DownLoadApater.MyViewHolder> {

    List<DownLoadModel> data = new ArrayList<>();
    Context context;

    public DownLoadApater(List<DownLoadModel> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_download, null));
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final DownloadInfo info = data.get(position).getInfo();

        holder.progressBar.setMax((int) info.getFileLength());
        holder.progressBar.setProgress((int) info.getProgress());
        holder.progressShow.setText((int) ((float) info.getProgress() / info.getFileLength() * 100) + "%");

        //按钮更新
        if (info.getState() == DownLoadManager.STATE_FINISH) {
            holder.downBtn.setText("重新開始");
            holder.prgressTv.setText(Double.toString(getRealNum(info.getProgress()/1024/1024)) + "MB/"
                    + Double.toString(getRealNum( info.getFileLength()/1024/1024)) + "MB"+" 下载完成");
        }

        if (info.getState() == DownLoadManager.STATE_WAITING) {
            holder.downBtn.setText("下载");
        }

        if (info.getState() == DownLoadManager.STATE_PAUSE) {
            holder.downBtn.setText("继续");
        }

        if (info.getState() == DownLoadManager.STATE_DOWNLOADING) {
            holder.downBtn.setText("暂停");
        }

        //add task
        LeopardHttp.DWONLOAD(info, new IProgress() {
            @Override
            public void onProgress(long progress, long total, boolean done) {

                //按钮更新

                if (info.getState() == DownLoadManager.STATE_WAITING) {
                    holder.downBtn.setText("下载");
                }

                if (info.getState() == DownLoadManager.STATE_PAUSE) {
                    holder.downBtn.setText("继续");
                }

                if (info.getState() == DownLoadManager.STATE_DOWNLOADING) {
                    holder.downBtn.setText("暂停");
                }

                holder.progressBar.setMax((int) total);
                holder.progressBar.setProgress((int) progress);
                holder.progressShow.setText((int) ((float) progress / total * 100) + "%");
                double curP = 0;
                double curTotal = 0;
                if (progress / 1024L < 1024L) {
                    curP = (double) progress / 1024;
                    curTotal = (double) total / 1024L / 1024L;
                    holder.prgressTv.setText(Double.toString(getRealNum(curP)) + "KB/" + Double.toString(getRealNum(curTotal)) + "MB"
                    );
                } else {
                    curP = (double) progress / 1024L / 1024L;
                    curTotal = (double) total / 1024L / 1024L;
                    holder.prgressTv.setText(Double.toString(getRealNum(curP)) + "MB/" + Double.toString(getRealNum(curTotal)) + "MB"
                    );
                }
                if (done) {
                    holder.downBtn.setText("重新開始");
                    holder.prgressTv.setText(holder.prgressTv.getText() + " 下載完成！");
                }
            }
        });


        holder.downBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (info.getState() == DownLoadManager.STATE_FINISH) {
                    holder.downBtn.setText("暫停");
                    info.getDownLoadTask().reStart();
                }

                if (info.getState() == DownLoadManager.STATE_WAITING) {
                    holder.downBtn.setText("暂停");
                    info.getDownLoadTask().reStart();
                    return;
                }

                if (info.getState() == DownLoadManager.STATE_PAUSE) {
                    holder.downBtn.setText("暫停");
                    data.get(position).getInfo().getDownLoadTask().resume();
                    return;
                }

                if (info.getState() == DownLoadManager.STATE_DOWNLOADING) {
                    holder.downBtn.setText("继续");
                    data.get(position).getInfo().getDownLoadTask().pause();
                    return;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    //获取保留的两位数
    private double getRealNum(double num) {
        BigDecimal bg = new BigDecimal(num);
        return bg.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        public Button downBtn;
        public ProgressBar progressBar;
        public TextView prgressTv;
        public TextView progressShow;

        public MyViewHolder(View itemView) {
            super(itemView);
            downBtn = (Button) itemView.findViewById(R.id.down_btn);
            progressBar = (ProgressBar) itemView.findViewById(R.id.down_pb);
            prgressTv = (TextView) itemView.findViewById(R.id.down_txt_pb);
            progressShow = (TextView) itemView.findViewById(R.id.down_progress);
        }

    }
}
