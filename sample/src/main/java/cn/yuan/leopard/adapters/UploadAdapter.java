package cn.yuan.leopard.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import cn.yuan.leopard.R;
import cn.yuan.leopard.Utils;
import cn.yuan.leopard.model.UploadModel;

/**
 * Created by Yuan on 2016/9/2.
 * Detail 上傳圖片適配器
 */
public class UploadAdapter extends RecyclerView.Adapter<UploadAdapter.MyViewHolder>{

    List<UploadModel> data = new ArrayList<>();
    IUploadGetPic iUploadGetPic;
    private Context context;

    public UploadAdapter(List<UploadModel> data, IUploadGetPic iUploadGetPic, Context context) {
        this.data = data;
        this.iUploadGetPic = iUploadGetPic;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_upoload,null));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        if (position!=0){
            String path = data.get(position).getFile().getPath();
//            Log.i("yuan", "show : " + path);
//            Bitmap bm = BitmapFactory.decodeFile(path);
            holder.imageView.setImageBitmap(Utils.getimage(path));
        }

        if (position == 0){
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iUploadGetPic.getPic();
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

      public ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.upload_img);
        }

    }

    public interface IUploadGetPic{

        public void getPic();

    }


}
