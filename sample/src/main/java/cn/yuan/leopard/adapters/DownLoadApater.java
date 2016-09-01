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
import com.yuan.leopardkit.interfaces.IProgress;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.yuan.leopard.R;
import cn.yuan.leopard.model.DownLoadModel;

/**
 * Created by Yuan on 2016/9/1.
 * Detail
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
        holder.downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.state == 0) {
                    holder.resetProgress();
                    holder.state = 2;
                    holder.downBtn.setText("暂停");
                    LeopardHttp.DWONLOAD(data.get(position).getInfo(), new IProgress() {
                        @Override
                        public void onProgress(long progress, long total, boolean done) {
                            if (holder.currentProgress == 0) {
                                holder.realSumSize = total;
                            }

                            if ( holder.state == 1){
                                return ;//防止異步影響
                            }

                            holder.currentProgress = progress+ holder.breakProgress;
                            holder.progressBar.setMax((int) holder.realSumSize );
                            holder.progressBar.setProgress((int)  holder.currentProgress);

                            double curP = 0;
                            double curTotal = 0;
                            if (holder.currentProgress / 1024L < 1024L) {
                                curP = (double) holder.currentProgress / 1024;
                                curTotal = (double) holder.realSumSize / 1024L / 1024L;
                                holder.prgressTv.setText(Double.toString(getRealNum(curP)) + "KB/" + Double.toString(getRealNum(curTotal)) + "MB");
                            } else {
                                curP = (double) holder.currentProgress / 1024L / 1024L;
                                curTotal = (double) holder.realSumSize / 1024L / 1024L;
                                holder.prgressTv.setText(Double.toString(getRealNum(curP)) + "MB/" + Double.toString(getRealNum(curTotal)) + "MB");
                            }
                            if (done) {
                                holder.downBtn.setText("重新開始");
                                holder.state = 0;
                                holder.prgressTv.setText(holder.prgressTv.getText() +" 下載完成！");
                            }
                        }
                    });
                    return;
                }

                if (holder.state == 1) {
                    holder.state = 2;
                    holder.downBtn.setText("暫停");
                    data.get(position).getInfo().getDownLoadTask().resume();
                    return;
                }

                if (holder.state == 2) {
                    holder.state = 1;
                    holder.downBtn.setText("继续");
                    holder.breakProgress = holder.currentProgress;
                    data.get(position).getInfo().getDownLoadTask().pause(holder.breakProgress);
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
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        public int state = 0;//0重新开始 1暂停 2 继续

        public long breakProgress = 0;
        public long currentProgress = 0;
        public long realSumSize = 0;

        public Button downBtn;
        public ProgressBar progressBar;
        public TextView prgressTv;

        public MyViewHolder(View itemView) {
            super(itemView);
            downBtn = (Button) itemView.findViewById(R.id.down_btn);
            progressBar = (ProgressBar) itemView.findViewById(R.id.down_pb);
            prgressTv = (TextView) itemView.findViewById(R.id.down_txt_pb);
        }

        public void resetProgress() {
            long breakProgress = 0;
            long currentProgress = 0;
            long realSumSize = 0;
        }
    }
}
