package cn.yuan.leopard.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yuan.leopardkit.LeopardHttp;
import com.yuan.leopardkit.download.DownLoadManager;
import com.yuan.leopardkit.download.model.DownloadInfo;
import com.yuan.leopardkit.interfaces.IDownloadProgress;

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

    int count = 0;

    public DownLoadApater(List<DownLoadModel> data, Context context) {
        this.data = data;
        this.context = context;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder viewholder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_download, null),count);
        return viewholder;
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        Log.i("onBindViewHolder", "onBindViewHolder: " + position +" "+ holder.getLayoutPosition() + " - " +holder.getAdapterPosition());

        final DownloadInfo info = data.get(position).getInfo();
        final DownLoadModel downLoadModel = data.get(position);

        //按钮更新
        if (info.getState() == DownLoadManager.STATE_FINISH) {
            holder.downBtn.setText("重新開始");
        } else if (info.getState() == DownLoadManager.STATE_WAITING) {// 默认状态
            holder.downBtn.setText("下载");
        }else if (info.getState() == DownLoadManager.STATE_PAUSE) {
            holder.downBtn.setText("继续");
        }else if (info.getState() == DownLoadManager.STATE_DOWNLOADING) {
            holder.downBtn.setText("暂停");
        }else{
            holder.downBtn.setText("下载");
        }

        // 默认状态更新
        if (info.getState() == DownLoadManager.STATE_FINISH || info.getState() == DownLoadManager.STATE_PAUSE){
            holder.progressBar.setMax((int) info.getFileLength());
            holder.progressBar.setProgress((int) info.getProgress());
            holder.progressShow.setText((int) ((float) info.getProgress() /  info.getFileLength() * 100) + "%");
            holder.prgressTv.setText(Double.toString(getRealNum(info.getProgress()/1024f/1024f)) + "MB/"
                    + Double.toString(getRealNum( info.getFileLength()/1024f/1024f)) + "MB"+" 下载完成");
        }else{

        }

        long result =  LeopardHttp.DWONLOAD(info, new IDownloadProgress() {
            @Override
            public void onProgress(long key,long progress, long total, boolean done) {

//                Log.i("yuan onProgress", " progress : " + progress + "now total :" + total);
                if (total == 0){
                    holder.progressShow.setText("0%");
                    return;
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

                if (progress >= total && total != 0) {
                    holder.downBtn.setText("重新開始");
                    holder.prgressTv.setText(holder.prgressTv.getText() + " 下載完成！");
                }
            }

            @Override
            public void onSucess(String result) {
                // nothing..
            }

            @Override
            public void onFailed(Throwable e, String reason) {
                // nothing..
                holder.downBtn.setText("继续");
                holder.prgressTv.setText(reason);
            }
        });

        holder.downBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (info.getState() == DownLoadManager.STATE_FINISH) {
                    holder.downBtn.setText("暫停");
                    DownLoadManager.getManager().restartTask(info);
                    return;
                }

                if (info.getState() == DownLoadManager.STATE_WAITING) {
                    holder.downBtn.setText("暂停");
                    DownLoadManager.getManager().restartTask(info);
                    return;
                }

                if (info.getState() == DownLoadManager.STATE_PAUSE) {
                    holder.downBtn.setText("暫停");
                    DownLoadManager.getManager().resumeTask(info);
                    return;
                }

                if (info.getState() == DownLoadManager.STATE_DOWNLOADING) {
                    holder.downBtn.setText("继续");
                    DownLoadManager.getManager().pauseTask(info);
                    return;
                }

                if (info.getState() == DownLoadManager.STATE_ERROR) {
                    holder.downBtn.setText("暫停");
                    DownLoadManager.getManager().resumeTask(info);
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

        public int position;
        public Button downBtn;
        public ProgressBar progressBar;
        public TextView prgressTv;
        public TextView progressShow;

        public MyViewHolder(View itemView,int position) {
            super(itemView);
            this.position = position;
            downBtn = (Button) itemView.findViewById(R.id.down_btn);
            progressBar = (ProgressBar) itemView.findViewById(R.id.down_pb);
            prgressTv = (TextView) itemView.findViewById(R.id.down_txt_pb);
            progressShow = (TextView) itemView.findViewById(R.id.down_progress);
        }

    }
}
